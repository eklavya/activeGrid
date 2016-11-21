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

  val PROCESS_TAG: String = "process"
  val ROLES_TAG_KEY: String = "roles"
  val APPLICATION_TAG_KEY = "stack"

  def findInstanceDetails: List[Instance] = {
    if (execute) {
      topology.nodes.map { instance =>
        val keypairOption = instance.sshAccessInfo.flatMap(_.keyPair)

        keypairOption.flatMap(keyPair =>
          keyPair.filePath.map(keyFilePath => {
            //Add AKKA for concurrent
            val userName = instance.sshAccessInfo.flatMap(ssh => ssh.userName)
              .orElse(keyPair.defaultUser)
              .fold(throw new IllegalArgumentException("userName is not available to perform SSH"))(name => name)

            val (newInstance, site) = collectInstanceDetails(topology.site, instance, userName, keyFilePath, keyPair.passPhrase)
            logger.info(s"Establishing service to collect instance details ${newInstance}")
            //TODO: site need to saved
            //topology.copy(site = site)
            newInstance
          })).getOrElse(instance)
      }
    } else topology.nodes
  }

  def collectInstanceDetails(site: Site1, instance: Instance, userName: String, keyLocation: String, passPharse: Option[String]): (Instance, Site1) = {
    val portOption = instance.sshAccessInfo.map(ssh => ssh.port)
    val hostProp: HostProperty = PrivateIpAndPublicDns

    val connections: List[Option[String]] = hostProp match {
      case PrivateIpAndPublicDns => List(instance.privateDnsName, instance.publicIpAddress)
      case PublicDns => List(instance.publicIpAddress)
      case PrivateIp => List(instance.privateDnsName)
    }
    connections.foldLeft((instance, site))((acc, connectionOption) => {
      connectionOption.map(connection => {
        val sshSession = new SSHSession(serverIp = connection,
          userName = userName,
          keyLocation = keyLocation,
          passPhrase = passPharse,
          port = portOption)
        grepInstanceDetails(sshSession, acc._1, acc._2)
      }).getOrElse(acc)
    })
  }

  private def grepInstanceDetails(sshSession: SSHSession, instance: Instance, site: Site1): (Instance, Site1) = {

    sshSession.start()
    logger.info("Establishing Ssh connection")
    //resolve OS and clear process tag
    val osInfo = sshSession.executeCommand("uname -ar")
    val tags = resolveOS(osInfo, instance).filter(tag => !tag.key.equals(PROCESS_TAG))

    //resolve Connections
    val connectionInfoOption = sshSession
      .executeCommand("sudo netstat -tnap | grep ESTABLISHED | awk 'BEGIN {OFS=\",\"; ORS= \";\"} NR > 0 {print substr($5, 0, index($5, \":\"))}'")
    val liveConnections = connectionResolver(connectionInfoOption, instance)
    logger.info(s"Live connections info $connectionInfoOption")

    val instance1 = instance.copy(liveConnections = liveConnections.toList, tags = tags)

    //resolveProcesses
    val processList = sshSession
      .executeCommand("sudo netstat -tnap | grep LISTEN | awk 'BEGIN {OFS=\",\"; ORS= \";\"} NR > 0 {print $7}'")
    logger.info(s"Process list is empty $processList")

    val (cloudInstanceResult, siteResult) =
      processList.map(processListValue => {
        resolveProcesses(site, instance1, toProcessMap(processListValue), sshSession)
      }).getOrElse(instance1, site)

    logger.info(s"Cloud instance  $cloudInstanceResult site $siteResult")
    sshSession.close()
    logger.info("Closing Ssh connection")
    (cloudInstanceResult, siteResult)
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
  private def toProcessMap(processList: String): Map[String, String] = {
    processList.split(";").map(process => {
      val index = process.indexOf("/")
      val pid = process.substring(0, index)
      val pName = process.substring(index + 1)
      (pid -> pName)
    }).toMap.filter(process => SoftwareProcess.isKnownProcess(process._2))
  }

  /* Find process name for JavaProcess and Python Process */
  private def findProcessName(sshSession: SSHSession, pName: String, pid: String): String = {
    SoftwareProcess.toSoftwareProcess(pName) match {
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

  private def resolveProcesses(site: Site1, instance: Instance, process: Map[String, String], sshSession: SSHSession): (Instance, Site1) = {
    // Find the process map
    // Iterate and add process details in Instance
    process.foldLeft((instance -> site))((accInstanceSite, process) => {
      val (pId, pName) = process
      val (instance, site) = accInstanceSite
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
      val instanceTags = KeyValueInfo(None, PROCESS_TAG, processName) :: instance.tags

      //Func: Add tag to instance and applications to site
      val (tags,apps) = softwareAssociatedWithProcessOpt.flatMap(software => {
        applicationDetails(instance,software,sshSession,pId,pName)
      }).getOrElse(instanceTags,List.empty)

      instance.copy(tags = instanceTags ++ tags,processes = instance.processes + processes ) -> site.copy(applications = apps)
    })
  }

  /* Get application details for the given instance with know software*/
  private def applicationDetails(instance:Instance,
                               software: Software,
                               sshSession: SSHSession,
                               pId: String,
                               pName: String): Option[(Set[KeyValueInfo],List[Application])]={
    val roleTagOption = setRole(instance, software)

    if (software.discoverApplications) {
      val apps = resolveApplication(sshSession, pId, pName, software)

      val appsTagOption = apps.map(app =>  KeyValueInfo(None, APPLICATION_TAG_KEY, app))
      val tags = roleTagOption.map(roleTag => appsTagOption + roleTag).getOrElse(appsTagOption)

      val applications = apps.map(app => {
        new Application(None, app, app, "1.0", List(instance), software, List.empty, /*None,*/ 0)
      }).toList
      Some((tags -> applications))
    } else None
  }

  /* Resolve application */
  private def resolveApplication(sshSession: SSHSession, pid: String, processName: String, software: Software): Set[String] = {
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

    SoftwareProcess.toSoftwareProcess(processName) match {
      case SoftwareProcess.JavaProcess => {
        val filesOption = fileInfoOption.map(_.split(";"))
        val applications: Array[String] = filesOption.map(files => {
          files.flatMap(file => file.split("\\|").flatMap(fileData => processFileName(fileData)))
        }).getOrElse(Array.empty)
        val knowSoftware = KnownSoftware.toKnownSoftware(software.name)
        val otherApplications = knowSoftware.applicationDiscoveryHelper.map(_.discoverApplications(pid, sshSession)).getOrElse(Set.empty)
        (applications ++ otherApplications).toSet
      }
      case _ => Set.empty
    }
  }

  private def setRole(instance: Instance, softwareAssociatedWithProcess: Software): Option[KeyValueInfo] = {
    val roleOption = InstanceRole.hasSoftware(softwareAssociatedWithProcess)
    val tagOption = instance.tags.find(tag => tag.value.equals(ROLES_TAG_KEY))
    roleOption.map(r => {
      tagOption.map(tag => {
        tag.copy(value = r.name)
      }).getOrElse(KeyValueInfo(None, ROLES_TAG_KEY, r.name))
    })
  }

  private def findSoftwares: List[Software] = {
    val softwareLabel: String = "SoftwaresTest2"
    val nodesList = Neo4jRepository.getNodesByLabel(softwareLabel)
    nodesList.flatMap(node => Software.fromNeo4jGraph(node.getId))
  }

  private def findSoftwareAssociatedWithProcess(processName: String): Option[Software] = {
    findSoftwares.find(software => software.processNames.contains(processName))
  }

  private def connectionResolver(connectionInfoOption: Option[String], instance: Instance): Set[InstanceConnection] = {
    connectionInfoOption.map(connectionInfo => {
      connectionInfo.split(";").toSet[String]
        .flatMap(otherNodeIp =>
          topology.getInstanceByIp(otherNodeIp)
            .filter(otherNode =>
              instance.instanceId.isDefined && otherNode.instanceId.isDefined)
            .map(otherNode => {
              new InstanceConnection(None, instance.instanceId.get, otherNode.instanceId.get, List.empty)
            }))
    }).getOrElse(Set.empty)
  }
}

sealed trait HostProperty

case object PrivateIp extends HostProperty

case object PublicDns extends HostProperty

case object PrivateIpAndPublicDns extends HostProperty
