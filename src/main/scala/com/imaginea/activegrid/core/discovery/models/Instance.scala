package com.imaginea.activegrid.core.discovery.models

import java.util.Date

import com.imaginea.activegrid.core.models._
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 10/10/16.
 */
case class Instance(override val id: Option[Long],
                    instanceId: String,
                    name: String,
                    state: String,
                    platform: String,
                    architecture: String,
                    publicDnsName: String/*,
                    launchTime: Date = new Date(),
                    memoryInfo: Option[StorageInfo] = None,
                    rootDiskInfo: Option[StorageInfo] = None,
                    tags: List[(String, String)] = List.empty,
                    sshAccessInfo: Option[SSHAccessInfo] = None,
                    liveConnections: List[InstanceConnection] = List.empty,
                    estimatedConnections: List[InstanceConnection] = List.empty,
                    processes: Set[ProcessInfo] = Set.empty,
                    imageInfo: Option[ImageInfo] = None,
                    existingUsers: List[InstanceUser] = List.empty*/
                     ) extends BaseEntity

object Instance {
  val label = "Instance"

  implicit class RichInstance(instance: Instance) extends Neo4jRep[Instance] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    val hasLaunchTime = "HAS_launchTime"
    val hasMemoryInfo = "HAS_memoryInfo"
    val hasApplications = "HAS_applications"
    val hasSshAccessInfo = "HAS_sshAccessInfo"
    val hasLiveConnections = "HAS_liveConnections"
    val hasEstimatedConnections = "HAS_estimatedConnections"
    val hasProcesses = "HAS_processes"
    val hasImageInfo = "HAS_imageInfo"
    val hasExistingUsers = "HAS_existingUsers"



    override def toNeo4jGraph(instance: Instance): Option[Node] = {

      logger.debug(s"Instance Node saved into db - ${instance}")
      val map = Map("instanceId" -> instance.instanceId
        ,"name" -> instance.name
        ,"state" -> instance.state
        ,"platform" -> instance.platform
        ,"architecture" -> instance.architecture
        ,"publicDnsName" -> instance.publicDnsName)

      val instanceNode = Neo4jRepository.saveEntity[UserGroup](label, instance.id, map)

      //map function is used to extract the option value
      //Iterating the users and linking to the UserGroup
      //logger.debug(s"Instances has relation with Site ${instance.siteName}")

      /*for {
        instance <- site.instances
        userNode <- instance.toNeo4jGraph(user)} {
        Neo4jRepository.createRelation(has_users, ugn, userNode)
      }

      //map function is used to extract the option value
      //Iterating the access and linking to the UserGroup
      logger.debug(s"UserGroupProxy has relation with ResourceACL ${userGroup.accesses}")
      for {accesses <- userGroup.accesses
           resource <- accesses
           ugn <- userGroupNode
           resourceNode <- resource.toNeo4jGraph(resource)} {
        Neo4jRepository.createRelation(has_resourceAccess, ugn, resourceNode)
      }*/

      instanceNode
    }


    override def fromNeo4jGraph(nodeId: Long): Option[Instance] = {
      val nodeOption = Neo4jRepository.findNodeById(nodeId)

      nodeOption.map(node => {
        logger.debug(s" UserGroupProxy ${node}")

        val instanceMap = Neo4jRepository.getProperties(node, "instanceId","name","state","platform","architecture","publicDnsName")

        /*val userNodes = Neo4jRepository.getNodesWithRelation(node, has_users)
        val users: Set[User] = userNodes.map(child => {
          logger.debug(s" UserGroup -> User node ${child}")
          val user: User = null
          user.fromNeo4jGraph(child.getId)
        }).flatten.toSet

        val accessNodes = Neo4jRepository.getNodesWithRelation(node, has_resourceAccess)
        val resources = accessNodes.map(child => {
          logger.debug(s" UserGroup -> Resource node ${child}")
          val resource: ResourceACL = null
          resource.fromNeo4jGraph(child.getId)
        }).flatten.toSet
        */
        val instance = Instance(
          id = Some(node.getId),
          instanceId = instanceMap.get("instanceId").get.asInstanceOf[String],
          name = instanceMap.get("name").get.asInstanceOf[String],
          state = instanceMap.get("state").get.asInstanceOf[String],
          platform = instanceMap.get("platform").get.asInstanceOf[String],
          architecture = instanceMap.get("architecture").get.asInstanceOf[String],
          publicDnsName = instanceMap.get("publicDnsName").get.asInstanceOf[String]
        )
        logger.debug(s"InstanceMap - ${instanceMap}")
        instance
      })
    }
  }
}
case class StorageInfo(override val id: Option[Long]
                       , used: Double
                       , total: Double) extends BaseEntity

case class SSHAccessInfo(override val id: Option[Long]
                         , keyPairKey: KeyPairInfo
                         , keyName: Option[String] = None
                         , port: Option[Int] = None
                          ) extends BaseEntity {
  def this(keyName: String) {
    this(None, new KeyPairInfo(id = None, keyName = keyName, status = NotYetUploadedKeyPair))
  }
}

case class InstanceConnection(override val id: Option[Long]
                              , sourceNodeId: String
                              , targetNodeId: String
                              , portRanges: List[PortRange]) extends BaseEntity {
  def this(sourceId: String, targetNodeId: String) {
    this(None, sourceId, targetNodeId, List.empty)
  }
}

case class PortRange(override val id: Option[Long],
                     fromPort: Int, toPort: Int) extends BaseEntity {

  def containsPort(port: Int) = port >= fromPort && port <= toPort

  override def toString: String = "RangeOfPorts [fromPort=" + fromPort + ", toPort=" + toPort + "]"
}

case class InstanceFlavor(override val id: Option[Long],
                          name: String,
                          cpuCount: Int,
                          memory: Double,
                          rootDisk: Double) extends BaseEntity

case class ProcessInfo(override val id: Option[Long]
                       , pid: Int
                       , parentPid: Int
                       , name: String
                       , software: Software
                       , softwareVersion: String) extends BaseEntity {

  val command: Option[String] = None
  val residentBytes: Option[Long] = None
}

case class Software(override val id: Option[Long]
                    , version: String
                    , name: String
                    , provider: String
                    , downloadURL: String
                    , port: String
                    , processNames: Array[String]
                    , discoverApplications: Boolean) extends BaseEntity
