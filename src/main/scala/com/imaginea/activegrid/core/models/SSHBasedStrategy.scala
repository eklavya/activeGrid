package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.SoftwareProcess.{JavaProcess, PythonProcess}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 3/11/16.
 */
case class SSHBasedStrategy(topology: Topology,
                            softwares: List[Software],
                            execute: Boolean
                             ) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val ProcessTag = "process"
  val RolesTagKey = "roles"
  val ApplicationTagKey = "stack"

  def getTopology: Topology = {
    val (nodes,apps) = execute match{
      case true => topology.nodes.foldLeft((List.empty[Instance],List.empty[Application])) { (acc,instance) =>
        val keypairOption = instance.sshAccessInfo.map(_.keyPair)
        val (accInstanceList,accApplications) = acc
        keypairOption.flatMap(keyPair =>
          keyPair.filePath.map(keyFilePath => {
            //Add AKKA for concurrent
            val userName = instance.sshAccessInfo.flatMap(ssh => ssh.userName)
              .orElse(keyPair.defaultUser)
              .fold(throw new IllegalArgumentException("UserName is not available to perform SSH"))(name => name)

            val (newInstance,applications) =
              collectInstanceDetails(topology.site, instance, userName, keyFilePath, keyPair
              .passPhrase)
            logger.info(s"Establishing service to collect instance details ${newInstance}")
            //TODO: site need to saved
            (newInstance :: accInstanceList) -> (applications ++ accApplications)
          })
        ).getOrElse(acc)
      }
      case false => (topology.nodes -> topology.site.applications)
    }
    val site = topology.site.copy(applications = apps)
    topology.copy(site = site,nodes = nodes)
  }

  def collectInstanceDetails(site: Site1,
                             instance: Instance,
                             userName: String,
                             keyLocation: String,
                             passPharse: Option[String]): (Instance,List[Application]) = {
    val portOption = instance.sshAccessInfo.flatMap(ssh => ssh.port)
    val hostProp: HostProperty = PrivateIpAndPublicDns

    val connections: List[Option[String]] = hostProp match {
      case PrivateIpAndPublicDns => List(instance.privateDnsName, instance.publicIpAddress)
      case PublicDns => List(instance.publicIpAddress)
      case PrivateIp => List(instance.privateDnsName)
    }
    connections.foldLeft((instance,List.empty[Application]))((acc, connectionOption) => {
      val (accInstance,accApplications) = acc
      connectionOption.map(connection => {
        val sshSession = new SSHSession1(serverIp = connection,
          userName = userName,
          keyLocation = keyLocation,
          passPhrase = passPharse,
          port = portOption)
        grepInstanceDetails(sshSession, accInstance,accApplications)
      }).getOrElse(acc)
    })
  }

  private def grepInstanceDetails(sshSession: SSHSession1, instance: Instance,applications:List[Application]):(Instance,List[Application])  = {

    sshSession.start()
    logger.info("Establishing Ssh connection")
    //resolve OS and clear process tag
    val osInfo = sshSession.executeCommand("uname -ar")
    val tags = resolveOS(osInfo, instance).filter(tag => !tag.key.equals(ProcessTag))

    //resolve Connections
    val connectionInfoOption = sshSession
      .executeCommand("sudo netstat -tnap | grep ESTABLISHED | awk 'BEGIN {OFS=\",\"; ORS= \";\"} NR > 0 {print substr($5, 0, index($5, \":\"))}'")
    val liveConnections = connectionResolver(connectionInfoOption, instance)
    logger.info(s"Live connections info $connectionInfoOption")

    val instance1 = instance.copy(liveConnections = liveConnections.toList, tags = tags)

    //resolveProcesses and Applications
    val processList = sshSession
      .executeCommand("sudo netstat -tnap | grep LISTEN | awk 'BEGIN {OFS=\",\"; ORS= \";\"} NR > 0 {print $7}'")
    logger.info(s"Process list is empty $processList")

    val (instanceResult,apps) =
      processList.map(processListValue => {
        resolveProcessesAndApplications(instance1, toProcessMap(processListValue), sshSession,applications)
      }).getOrElse(instance1,applications)

    logger.info(s"Cloud instance  $instanceResult")
    sshSession.close()
    logger.info("Closing Ssh connection")
    instanceResult -> apps
  }

  private def resolveOS(osInfo: Option[String], instance: Instance): List[KeyValueInfo] = {
    osInfo match {
      case Some(osInfoValue) if (osInfoValue.toLowerCase().contains("ubuntu")) =>
        KeyValueInfo(None, "osinfo", "ubuntu") :: instance.tags
      case _ => instance.tags
    }
  }

  /*
   * Collect map of process id and name and
   * filter with know software process
   */
  private def toProcessMap(processList: String) = {
    processList.split(";").map(process => {
      val index = process.indexOf("/")
      val pid = process.substring(0, index)
      val pName = process.substring(index + 1)
      (pid -> pName)
    }).filter(process => SoftwareProcess.isKnownProcess(process._2)).toMap
  }

  /* Find process name for JavaProcess and Python Process */
  private def findProcessName(sshSession: SSHSession1, pName: String, pid: String): String = {
    SoftwareProcess.toProcessType(pName) match {
      case JavaProcess => {
        val processInfo = sshSession.executeCommand("ps -p " + pid
          + " -o args=ARGS | awk 'BEGIN {OFS=\",\"; ORS= \";\"} NR > 1'"); // -o
        // vsz=MEMORY
        processInfo.filter(process => {
          KnownSoftware.getStartUpClazzMap.contains(process)
        }).head
      }
      case PythonProcess => {
        val procInfo = sshSession.executeCommand("ps -p " + pid
          + " | grep -v CMD | awk '{print $4}'")
        val result = if (procInfo.contains("carbon-cache")) {
          val chkLb = "sudo netstat -apnt |grep LISTEN|grep :80|awk 'BEGIN {OFS=\",\";ORS=\";\"} NR > 0 {print $4}'"
          val netStatInfo = sshSession.executeCommand(chkLb)
          netStatInfo.map(netStat => {
            netStat.split(";").filter(host => host.endsWith(":80")).map(tmp => KnownSoftware.Graphite.name).head
          }).getOrElse(pName)
        } else {
          pName
        }
        result
      }
      case _ => pName
    }
  }

  private def resolveProcessesAndApplications(instance: Instance,
                                              process: Map[String, String],
                                              sshSession: SSHSession1,
                                              applications:List[Application]):(Instance,List[Application]) = {
    // Find the process map
    // Iterate and add process details in Instance
    process.foldLeft(instance,applications)((acc, process) => {
      val (pId, pName) = process
      val (instance,applications) = acc
      //Find process name with clazz name
      val processName = findProcessName(sshSession, pId, pName)

      // Collect all software which associated with process name
      val softwareAssociatedWithProcessOpt = findSoftwareAssociatedWithProcess(processName)

      //Update processInfo and tags in Instance
      //Func: Add process to list and Add tags list
      val processes = new ProcessInfo(pid = Option(pId.toInt)
        , parentPid = Option(pId.toInt)
        , name = Option(processName)
        , software = softwareAssociatedWithProcessOpt
        , id = None
        , command = None
        , owner = None
        , residentBytes = None
        , softwareVersion = None
      )
      val instanceTags = KeyValueInfo(None, ProcessTag, processName) :: instance.tags
      val (appTags ,apps) = applicationDetails(instance,sshSession,pId,pName)
      //FixMe: Application and tags need to be added in instance and site
      instance.copy(tags = instanceTags ++ appTags, processes = instance.processes + processes) -> (apps ++ applications)
    })
  }

  /* Get application details for the given instance with know software*/
  private def applicationDetails(instance:Instance,
                                 sshSession: SSHSession1,
                                 pId: String,
                                 pName: String): (Set[KeyValueInfo],List[Application])={
    softwares.foldLeft((Set.empty[KeyValueInfo],List.empty[Application]))( (acc ,software) => {
      val roleTagOption = roleTag(instance, software)
      val (accTags,accApps) = acc
      val result =
        if (software.discoverApplications) {
          val apps = resolveApplication(sshSession, pId, pName, software)
          val appsTagOption = apps.map(app =>  KeyValueInfo(None, ApplicationTagKey, app))
          val tags = roleTagOption.map(roleTag => appsTagOption + roleTag).getOrElse(appsTagOption)

          val applications = apps.map(app => {
            new Application(None, Some(app), Some(app), Some("1.0"), List(instance), None, List.empty,None, Some(0))
          }).toList
          (tags ++ accTags) -> ( accApps ++ applications)
      } else {
          acc
      }
      result
    })
  }

  /* Resolve application */
  private def resolveApplication(sshSession: SSHSession1, pid: String, processName: String, software: Software): Set[String] = {
    def processFileName(fileName: String): Option[String] = {
      val f = fileName.trim
       (f.contains("/WEB-INF")) match {
         case true => f.split("/WEB-INF")(0)
           Some(f.substring(f.lastIndexOf("/") + 1))
         case false => None
       }
    }
    val fileInfoOption = sshSession.executeCommand("sudo lsof -p " + pid
      + "| awk 'BEGIN {OFS=\"|\";ORS=\";\"} NR > 0 {print $3,$9,$7}'")

    SoftwareProcess.toProcessType(processName) match {
      case SoftwareProcess.JavaProcess => {
        val filesOption = fileInfoOption.map(_.split(";"))
        val applications: Array[String] = filesOption.map(files => {
          files.flatMap(file => file.split("\\|").flatMap(fileData => processFileName(fileData)))
        }).getOrElse(Array.empty)
        val knowSoftware = KnownSoftware.toSoftwareType(software.name)
        val otherApplications = knowSoftware.applicationDiscoveryHelper.map(_.discoverApplications(pid, sshSession)).getOrElse(Set.empty)
        (applications ++ otherApplications).toSet
      }
      case _ => Set.empty
    }
  }

  private def roleTag(instance: Instance, software: Software): Option[KeyValueInfo] = {
    val roleOption = InstanceRole.hasSoftware(software)
    val tagOption = instance.tags.find(tag => tag.value.equals(RolesTagKey))
    roleOption.map(r => {
      tagOption.map(tag => {
        tag.copy(value = r.name)
      }).getOrElse(KeyValueInfo(None, RolesTagKey, r.name))
    })
  }

  private def findSoftwareAssociatedWithProcess(processName: String): Option[Software] = {
    softwares.find(software => software.processNames.contains(processName))
  }

  /* Connection Resolver will return the Instance connection
   * It get the ssh result as connectionInformation and instance
   * iterate the connection information and check for instance with same ip from topology nodes
   * */
  private def connectionResolver(connectionInfoOption: Option[String], instance: Instance): Set[InstanceConnection] = {
    connectionInfoOption.map(connectionInfo => {
      val connectionSet = connectionInfo.split(";").toSet[String]
      for {otherNodeIp <- connectionSet
           otherNode <- topology.getInstanceByIp(otherNodeIp)
           otherInstanceId <- otherNode.instanceId
           instanceId <- instance.instanceId
      } yield (new InstanceConnection(None, instanceId, otherInstanceId, List.empty))
    }).getOrElse(Set.empty)
  }
}

sealed trait HostProperty

case object PrivateIp extends HostProperty

case object PublicDns extends HostProperty

case object PrivateIpAndPublicDns extends HostProperty
