package com.activegrid.model

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class Instance(override val id: Option[Long],
                    instanceId: Option[String],
                    name: String,
                    state: Option[String],
                    instanceType: Option[String],
                    platform: Option[String],
                    architecture: Option[String],
                    publicDnsName: Option[String],
                    launchTime: Option[Long],
                    memoryInfo: Option[StorageInfo],
                    rootDiskInfo: Option[StorageInfo],
                    tags: List[Tuple],
                    sshAccessInfo: Option[SSHAccessInfo],
                    liveConnections: List[InstanceConnection],
                    estimatedConnections: List[InstanceConnection],
                    processes: Set[ProcessInfo],
                    image: Option[ImageInfo],
                    existingUsers: List[InstanceUser]
                   ) extends BaseEntity


object Instance {

  def apply(name: String,tags: List[Tuple],processes: Set[ProcessInfo] ): Instance =

    Instance(None, None, name, None, None, None, None, None, None, None, None, tags, None, List.empty[InstanceConnection], List.empty[InstanceConnection], processes, None, List.empty[InstanceUser])

  def apply(name: String): Instance =

    Instance(None, None, name, None, None, None, None, None, None, None, None, List.empty[Tuple], None, List.empty[InstanceConnection], List.empty[InstanceConnection], Set.empty[ProcessInfo], None, List.empty[InstanceUser])

  implicit class InstanceImpl(instance: Instance) extends Neo4jRep[Instance] {

    override def toNeo4jGraph(entity: Instance): Option[Node] = {

      val logger = Logger(LoggerFactory.getLogger(getClass.getName))
      val label = "Instance"

      val mapPrimitives = Map("instanceId" -> entity.instanceId.getOrElse(GraphDBExecutor.NO_VAL),
        "name" -> entity.name,
        "state" -> entity.state.getOrElse(GraphDBExecutor.NO_VAL),
        "instanceType" -> entity.instanceType.getOrElse(GraphDBExecutor.NO_VAL),
        "platform" -> entity.platform.getOrElse(GraphDBExecutor.NO_VAL),
        "architecture" -> entity.architecture.getOrElse(GraphDBExecutor.NO_VAL),
        "publicDnsName" -> entity.publicDnsName.getOrElse(GraphDBExecutor.NO_VAL),
        "launchTime" -> entity.launchTime.getOrElse(GraphDBExecutor.NO_VAL))

      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[Instance](label, mapPrimitives)

      //TO BE DONE
      /*    val dateNode: Option[Node] = entity.launchTime.toGraph(entity.launchTime)
            val relationship_date = "HAS_date"
            GraphDBExecutor.setGraphRelationship(node,dateNode,relationship_date)
      */
//14th Oct
      /*
      val memoryInfoNode: Option[Node] = entity.memoryInfo.toNeo4jGraph(entity.memoryInfo)
      val relationship_storage1 = "HAS_storageInfo1"
      GraphDBExecutor.setGraphRelationship(node, memoryInfoNode, relationship_storage1)

      val rootDiskInfoNode: Option[Node] = entity.rootDiskInfo.toNeo4jGraph(entity.rootDiskInfo)
      val relationship_storage2 = "HAS_storageInfo2"
      GraphDBExecutor.setGraphRelationship(node, rootDiskInfoNode, relationship_storage2)

      val sshAccessInfoNode: Option[Node] = entity.sshAccessInfo.toNeo4jGraph(entity.sshAccessInfo)
      val relationship_ssh = "HAS_sshAccessInfo"
      GraphDBExecutor.setGraphRelationship(node, sshAccessInfoNode, relationship_ssh)

      val imageInfoNode: Option[Node] = entity.image.toNeo4jGraph(entity.image)
      val relationship_image = "HAS_imageInfo"
      GraphDBExecutor.setGraphRelationship(node, imageInfoNode, relationship_image)*/

      entity.memoryInfo match {
        case Some(mInfo) => {
          val memoryInfoNode = mInfo.toNeo4jGraph(mInfo)
          val relationship_storage1 = "HAS_storageInfo1"
          GraphDBExecutor.setGraphRelationship(node, memoryInfoNode, relationship_storage1)
        }
        case None => logger.debug("entity Instance has no memoryInfo")
      }

      entity.rootDiskInfo match  {
        case Some(rInfo) =>{
          val rootDiskInfoNode = rInfo.toNeo4jGraph(rInfo)
          val relationship_storage2 = "HAS_storageInfo2"
          GraphDBExecutor.setGraphRelationship(node, rootDiskInfoNode, relationship_storage2)
        }
        case None => logger.info("entity Instance has no rootDiskInfo")
      }

      entity.sshAccessInfo match {
        case Some(ssh)=>{
          val sshAccessInfoNode = ssh.toNeo4jGraph(ssh)
          val relationship_ssh = "HAS_sshAccessInfo"
          GraphDBExecutor.setGraphRelationship(node, sshAccessInfoNode, relationship_ssh)
        }
        case None =>  logger.info("entity Instance has no sshAccessInfo")
      }

      entity.image match {
        case Some(image) =>{
          val imageInfoNode = image.toNeo4jGraph(image)
          val relationship_image = "HAS_imageInfo"
          GraphDBExecutor.setGraphRelationship(node, imageInfoNode, relationship_image)
        }
        case None =>  logger.info("entity Instance has no imageInfo")
      }

      val relationship_tuple = "HAS_tuple"
      entity.tags.foreach { tag =>
        val tagNode = tag.toNeo4jGraph(tag)
        GraphDBExecutor.setGraphRelationship(node, tagNode, relationship_tuple)
      }

      val relationship_inst1 = "HAS_instanceConnection1"
      entity.liveConnections.foreach { liveConnection =>
        val liveConnectionNode = liveConnection.toNeo4jGraph(liveConnection)
        GraphDBExecutor.setGraphRelationship(node, liveConnectionNode, relationship_inst1)
      }

      val relationship_inst2 = "HAS_instanceConnection2"
      entity.estimatedConnections.foreach { estimatedConnection =>
        val estimatedConnectionNode = estimatedConnection.toNeo4jGraph(estimatedConnection)
        GraphDBExecutor.setGraphRelationship(node, estimatedConnectionNode, relationship_inst2)
      }

      val relationship_instuser = "HAS_instanceUser"
      entity.existingUsers.foreach { existingUser =>
        val existingUserNode = existingUser.toNeo4jGraph(existingUser)
        GraphDBExecutor.setGraphRelationship(node, existingUserNode, relationship_instuser)
      }

      val relationship_process = "HAS_processInfo"
      entity.processes.foreach { process =>
        val processNode = process.toNeo4jGraph(process)
        GraphDBExecutor.setGraphRelationship(node, processNode, relationship_process)
      }

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[Instance] = {

     Instance.fromNeo4jGraph(id)

    }

  }

  def fromNeo4jGraph(id: Option[Long]): Option[Instance] = {

    id match {
      case Some(nodeId) => {
        val listOfKeys = List("instanceId", "name", "state", "instanceType", "platform", "architecture", "publicDnsName")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val instanceId = propertyValues.get("instanceId").asInstanceOf[Option[String]]
        val name = propertyValues.get("name").get.toString
        val state = propertyValues.get("state").asInstanceOf[Option[String]]
        val instanceType = propertyValues.get("instanceType").asInstanceOf[Option[String]]
        val platform = propertyValues.get("platform").asInstanceOf[Option[String]]
        val architecture = propertyValues.get("architecture").asInstanceOf[Option[String]]
        val publicDnsName = propertyValues.get("publicDnsName").asInstanceOf[Option[String]]
        //TO DO
        //val launchTime: Date = new Date(propertyValues.get("launchTime").get.toString.toLong)
        val launchTime: Option[Long] = Some(100)
        //propertyValues.get("launchTime").get.toString.toLong

        val relationship_info1 = "HAS_storageInfo1"
        val childNodeId_info1 = GraphDBExecutor.getChildNodeId(nodeId, relationship_info1)

        val memoryInfo: Option[StorageInfo] = StorageInfo.fromNeo4jGraph(childNodeId_info1)

        val relationship_info2 = "HAS_storageInfo2"
        val childNodeId_info2 = GraphDBExecutor.getChildNodeId(nodeId, relationship_info2)

        val rootDiskInfo: Option[StorageInfo] = StorageInfo.fromNeo4jGraph(childNodeId_info2)

        val relationship_ssh = "HAS_sshAccessInfo"
        val childNodeId_ssh = GraphDBExecutor.getChildNodeId(nodeId, relationship_ssh)

        val sshAccessInfo: Option[SSHAccessInfo] = SSHAccessInfo.fromNeo4jGraph(childNodeId_ssh)

        val relationship_image = "HAS_imageInfo"
        val childNodeId_image = GraphDBExecutor.getChildNodeId(nodeId, relationship_image)

        val imageInfo: Option[ImageInfo] = ImageInfo.fromNeo4jGraph(childNodeId_image)

        val relationship_tuple = "HAS_tuple"
        val childNodeIds_tuple: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_tuple)

        val tags: List[Tuple] = childNodeIds_tuple.map { childId =>
          Tuple.fromNeo4jGraph(Some(childId)).get
        }

        val relationship_inst1 = "HAS_instanceConnection1"
        val childNodeIds_inst1: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_inst1)

        val liveConnections: List[InstanceConnection] = childNodeIds_inst1.map { childId =>
          InstanceConnection.fromNeo4jGraph(Some(childId)).get
        }

        val relationship_inst2 = "HAS_instanceConnection2"
        val childNodeIds_inst2: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_inst2)

        val estimatedConnections: List[InstanceConnection] = childNodeIds_inst2.map { childId =>
          InstanceConnection.fromNeo4jGraph(Some(childId)).get
        }

        val relationship_user = "HAS_instanceUser"
        val childNodeIds_user: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_user)

        val existingUsers: List[InstanceUser] = childNodeIds_user.map { childId =>
          InstanceUser.fromNeo4jGraph(Some(childId)).get
        }

        val relationship_process = "HAS_processInfo"
        val childNodeIds_process: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_process)

        val processes: Set[ProcessInfo] = childNodeIds_process.map { childId =>
          ProcessInfo.fromNeo4jGraph(Some(childId)).get
        }.toSet

        Some(Instance(Some(nodeId), instanceId, name, state, instanceType, platform, architecture, publicDnsName, launchTime, memoryInfo, rootDiskInfo,
          tags, sshAccessInfo, liveConnections, estimatedConnections, processes, imageInfo, existingUsers))

      }
      case None => None
    }

  }

}
