package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class Instance(override val id: Option[Long],
                    instanceId: String,
                    name: String,
                    state: String,
                    instanceType: String,
                    platform: String,
                    architecture: String,
                    publicDnsName: String,
                    launchTime: Long,
                    memoryInfo: StorageInfo,
                    rootDiskInfo: StorageInfo,
                    tags: List[Tuple],
                    sshAccessInfo: SSHAccessInfo,
                    liveConnections: List[InstanceConnection],
                    estimatedConnections: List[InstanceConnection],
                    processes: Set[ProcessInfo],
                    image: ImageInfo,
                    existingUsers: List[InstanceUser]
                   ) extends BaseEntity


object Instance{

  implicit class InstanceImpl(instance: Instance) extends Neo4jRep[Instance]{

    override def toNeo4jGraph(entity: Instance): Option[Node] = {

      val label  = "Instance"

      val mapPrimitives = Map("instanceId" -> entity.instanceId,
        "name"        -> entity.name,
        "state"       -> entity.state,
        "instanceType"-> entity.instanceType,
        "platform"      -> entity.platform,
        "architecture"  -> entity.architecture,
        "publicDnsName" -> entity.publicDnsName,
        "launchTime"    -> entity.launchTime)

      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[TestImplicit](label, mapPrimitives)

      //TO BE DONE
      /*    val dateNode: Option[Node] = entity.launchTime.toGraph(entity.launchTime)
            val relationship_date = "HAS_date"
            GraphDBExecutor.setGraphRelationship(node,dateNode,relationship_date)
      */

      val memoryInfoNode: Option[Node] = entity.memoryInfo.toNeo4jGraph(entity.memoryInfo)
      val relationship_storage1 = "HAS_storageInfo1"
      GraphDBExecutor.setGraphRelationship(node,memoryInfoNode,relationship_storage1)

      val rootDiskInfoNode: Option[Node] = entity.rootDiskInfo.toNeo4jGraph(entity.rootDiskInfo)
      val relationship_storage2 = "HAS_storageInfo2"
      GraphDBExecutor.setGraphRelationship(node,rootDiskInfoNode,relationship_storage2)

      val sshAccessInfoNode: Option[Node] = entity.sshAccessInfo.toNeo4jGraph(entity.sshAccessInfo)
      val relationship_ssh = "HAS_sshAccessInfo"
      GraphDBExecutor.setGraphRelationship(node,sshAccessInfoNode,relationship_ssh)

      val imageInfoNode: Option[Node] = entity.image.toNeo4jGraph(entity.image)
      val relationship_image = "HAS_imageInfo"
      GraphDBExecutor.setGraphRelationship(node,imageInfoNode,relationship_image)

      val relationship_tuple = "HAS_tuple"
      entity.tags.foreach{tag =>
        val tagNode = tag.toNeo4jGraph(tag)
        GraphDBExecutor.setGraphRelationship(node,tagNode,relationship_tuple)
      }

      val relationship_inst1 = "HAS_instanceConnection1"
      entity.liveConnections.foreach{liveConnection =>
        val liveConnectionNode = liveConnection.toNeo4jGraph(liveConnection)
        GraphDBExecutor.setGraphRelationship(node,liveConnectionNode,relationship_inst1)
      }

      val relationship_inst2 = "HAS_instanceConnection2"
      entity.estimatedConnections.foreach{estimatedConnection =>
        val estimatedConnectionNode = estimatedConnection.toNeo4jGraph(estimatedConnection)
        GraphDBExecutor.setGraphRelationship(node,estimatedConnectionNode,relationship_inst2)
      }

      val relationship_instuser = "HAS_instanceUser"
      entity.existingUsers.foreach{existingUser =>
        val existingUserNode = existingUser.toNeo4jGraph(existingUser)
        GraphDBExecutor.setGraphRelationship(node,existingUserNode,relationship_instuser)
      }

      val relationship_process = "HAS_processInfo"
      entity.processes.foreach{process=>
        val processNode = process.toNeo4jGraph(process)
        GraphDBExecutor.setGraphRelationship(node,processNode,relationship_process)
      }

      node

    }

    override def fromNeo4jGraph(nodeId: Long): Instance = {

      val listOfKeys = List("instanceId","name","state","instanceType", "platform", "architecture", "publicDnsName")
      val propertyValues = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val instanceId = propertyValues.get("instanceId").get.toString
      val name = propertyValues.get("name").get.toString
      val state = propertyValues.get("state").get.toString
      val instanceType = propertyValues.get("instanceType").get.toString
      val platform = propertyValues.get("platform").get.toString
      val architecture = propertyValues.get("architecture").get.toString
      val publicDnsName = propertyValues.get("publicDnsName").get.toString
      //TO DO
      //val launchTime: Date = new Date(propertyValues.get("launchTime").get.toString.toLong)
      val launchTime = 100
      //propertyValues.get("launchTime").get.toString.toLong

      val relationship_info1 = "HAS_storageInfo1"
      val childNodeId_info1 = GraphDBExecutor.getChildNodeId(nodeId,relationship_info1)

      val storageInfo: StorageInfo = null
      val memoryInfo: StorageInfo = storageInfo.fromNeo4jGraph(childNodeId_info1)

      val relationship_info2 = "HAS_storageInfo2"
      val childNodeId_info2 = GraphDBExecutor.getChildNodeId(nodeId,relationship_info2)

      //      val storageInfo: StorageInfo = null
      val rootDiskInfo: StorageInfo = storageInfo.fromNeo4jGraph(childNodeId_info2)

      val relationship_ssh = "HAS_sshAccessInfo"
      val childNodeId_ssh = GraphDBExecutor.getChildNodeId(nodeId,relationship_ssh)

      val ssh: SSHAccessInfo = null
      val sshAccessInfo: SSHAccessInfo = ssh.fromNeo4jGraph(childNodeId_ssh)

      val relationship_image = "HAS_imageInfo"
      val childNodeId_image = GraphDBExecutor.getChildNodeId(nodeId,relationship_image)

      val image:ImageInfo = null
      val imageInfo: ImageInfo = image.fromNeo4jGraph(childNodeId_image)

      val relationship_tuple = "HAS_tuple"
      val childNodeIds_tuple: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_tuple)

      val tags: List[Tuple] = childNodeIds_tuple.map{ childId =>
        val tuple:Tuple = null
        tuple.fromNeo4jGraph(childId)
      }

      val relationship_inst1 = "HAS_instanceConnection1"
      val childNodeIds_inst1: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_inst1)

      val liveConnections: List[InstanceConnection] = childNodeIds_inst1.map{ childId =>
        val inst:InstanceConnection = null
        inst.fromNeo4jGraph(childId)
      }

      val relationship_inst2 = "HAS_instanceConnection2"
      val childNodeIds_inst2: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_inst2)

      val estimatedConnections: List[InstanceConnection] = childNodeIds_inst2.map{ childId =>
        val inst:InstanceConnection = null
        inst.fromNeo4jGraph(childId)
      }

      val relationship_user = "HAS_instanceUser"
      val childNodeIds_user: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_user)

      val existingUsers: List[InstanceUser] = childNodeIds_user.map{ childId =>
        val user:InstanceUser = null
        user.fromNeo4jGraph(childId)
      }

      val relationship_process = "HAS_processInfo"
      val childNodeIds_process: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_process)

      val processes: Set[ProcessInfo] = childNodeIds_process.map{ childId =>
        val info:ProcessInfo = null
        info.fromNeo4jGraph(childId)
      }.toSet

      Instance(Some(nodeId),instanceId, name, state,instanceType, platform, architecture, publicDnsName, launchTime, memoryInfo, rootDiskInfo,
        tags, sshAccessInfo,  liveConnections, estimatedConnections, processes,imageInfo, existingUsers)
    }

  }

}
