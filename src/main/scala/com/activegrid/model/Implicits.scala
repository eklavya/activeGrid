package com.activegrid.model

import akka.http.scaladsl.model.headers.Date
import com.activegrid.model.Graph.Neo4jRep
import com.activegrid.model.KeyPairStatus.KeyPairStatus
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

/**
  * Created by shareefn on 3/10/16.
  */
object Implicits {


  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    override def toGraph(entity: ImageInfo): Option[Node] = {

      val label: String = "ImageInfo"

     val node = GraphDBExecutor.createGraphNode[ImageInfo](entity, label)
      node
    }

    override def fromGraph(nodeId: Long): Option[ImageInfo] = {


      GraphDBExecutor.getEntity[ImageInfo](nodeId)


    }

  }


  implicit class TestImplicitImpl(testImplicit: TestImplicit) extends Neo4jRep[TestImplicit] {
    override def toGraph(entity: TestImplicit): Option[Node] = {


      val label: String = "TestImplicit"

      val mapPrimitives : Map[String, Any] = Map("id" -> entity.id, "name" -> entity.name)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

      GraphDBExecutor.setGraphProperties(node.get,"set_property_test","success")

      val node2: Option[Node] = entity.image.toGraph(entity.image)


      val relationship = "HAS_image"
      GraphDBExecutor.setGraphRelationship(node,node2,relationship)

      node

    }

    override def fromGraph(nodeId: Long): Option[TestImplicit] = {

      val listOfKeys: List[String] = List("id","name")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val id: Long = propertyValues.get("id").get.toString.toLong
      val name: String = propertyValues.get("name").get.toString

      val relationship = "HAS_image"
      val childNodeId = GraphDBExecutor.getChildNodeId(nodeId,relationship)

      val image:ImageInfo = null
      val imageInfo: Option[ImageInfo] = image.fromGraph(childNodeId)
      println(s"childNodeId = ${childNodeId}")
      println(s"id = ${id}, name = ${name} , imageInfo = ${imageInfo.getOrElse("ab")}")
Some(TestImplicit(id, name ,imageInfo.get))
//      Some(TestImplicit(id,name ,ImageInfo("","","",true,"","","","","","","","","")))
    }
  }

implicit class StorageInfoImpl(storageInfo: StorageInfo) extends Neo4jRep[StorageInfo]{

  override def toGraph(entity: StorageInfo): Option[Node] = {

    val label: String = "StorageInfo"

    val node = GraphDBExecutor.createGraphNode[StorageInfo](entity, label)
    node

  }

  override def fromGraph(nodeId: Long): Option[StorageInfo] = {

    GraphDBExecutor.getEntity[StorageInfo](nodeId)
  }

}

  implicit class TupleImpl(tuple: Tuple) extends Neo4jRep[Tuple]{

    override def toGraph(entity: Tuple): Option[Node] = {

      val label: String = "Tuple"

      val node = GraphDBExecutor.createGraphNode[Tuple](entity, label)
      node

    }

    override def fromGraph(nodeId: Long): Option[Tuple] = {

      GraphDBExecutor.getEntity[Tuple](nodeId)
    }

  }

  implicit class InstanceUserImpl(instanceUser: InstanceUser) extends Neo4jRep[InstanceUser]{

    override def toGraph(entity: InstanceUser): Option[Node] = {

      val label: String = "InstanceUser"

      val mapPrimitives : Map[String, Any] = Map("userName" -> entity.userName, "publicKeys" -> entity.publicKeys.toArray)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

      node
    }

    override def fromGraph(nodeId: Long): Option[InstanceUser] = {

      val listOfKeys: List[String] = List("userName","publicKeys")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val userName: String = propertyValues.get("userName").get.toString
      val publicKeys: List[String] = propertyValues.get("publicKeys").get.asInstanceOf[Array[String]].toList

      Some(InstanceUser(userName,publicKeys))
    }

  }

  implicit class PortRangeImpl(portRange: PortRange) extends Neo4jRep[PortRange]{

    override def toGraph(entity: PortRange): Option[Node] = {

      val label: String = "PortRange"

      val node = GraphDBExecutor.createGraphNode[PortRange](entity, label)
      node

    }

    override def fromGraph(nodeId: Long): Option[PortRange] = {

      GraphDBExecutor.getEntity[PortRange](nodeId)
    }

  }

  implicit class InstanceConnectionImpl(instanceConnection: InstanceConnection) extends Neo4jRep[InstanceConnection]{

    override def toGraph(entity: InstanceConnection): Option[Node] = {

      val label: String = "InstanceConnection"

      val mapPrimitives : Map[String, Any] = Map("sourceNodeId" -> entity.sourceNodeId, "targetNodeId" -> entity.targetNodeId)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

      val relationship = "HAS_portRange"
      entity.portRanges.foreach{portRange =>

        val portRangeNode = portRange.toGraph(portRange)
        GraphDBExecutor.setGraphRelationship(node,portRangeNode,relationship)

      }

      node
    }

    override def fromGraph(nodeId: Long): Option[InstanceConnection] = {

      val listOfKeys: List[String] = List("sourceNodeId","targetNodeId")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val sourceNodeId: String = propertyValues.get("sourceNodeId").get.toString
      val targetNodeId: String = propertyValues.get("targetNodeId").get.toString


      val relationship = "HAS_portRange"
      val childNodeIds: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship)

      val portRanges: List[PortRange] = childNodeIds.map{ childId =>
                                                        val port:PortRange = null
                                                        port.fromGraph(childId).get
                                                        }

      Some(InstanceConnection(sourceNodeId,targetNodeId,portRanges))
    }

  }

  implicit class SoftwareImpl(software: Software) extends Neo4jRep[Software]{

    override def toGraph(entity: Software): Option[Node] = {

      val label: String = "Software"

      val mapPrimitives : Map[String, Any] = Map( "version"     -> entity.version,
                                                  "name"        -> entity.name,
                                                  "provider"    -> entity.provider,
                                                  "downloadURL" -> entity.downloadURL,
                                                  "port"        -> entity.port,
                                                  "processNames"-> entity.processNames.toArray,
                                                  "discoverApplications" -> entity.discoverApplications)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

      node
    }

    override def fromGraph(nodeId: Long): Option[Software] = {

      val listOfKeys: List[String] = List("version","name","provider","downloadURL","port","processNames","discoveryApplications")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val version: String = propertyValues.get("version").get.toString
      val name: String = propertyValues.get("name").get.toString
      val provider: String = propertyValues.get("provider").get.toString
      val downloadURL: String = propertyValues.get("downloadURL").get.toString
      val port: String = propertyValues.get("port").get.toString
      val processNames: List[String] = propertyValues.get("processNames").get.asInstanceOf[Array[String]].toList
      val discoverApplications: Boolean = propertyValues.get("discoverApplications").get.toString.toBoolean
      Some(Software(version, name, provider, downloadURL, port, processNames,discoverApplications))
    }

  }

  implicit class ProcessInfoImpl(processInfo: ProcessInfo) extends Neo4jRep[ProcessInfo] {
    override def toGraph(entity: ProcessInfo): Option[Node] = {


      val label: String = "ProcessInfo"

      val mapPrimitives : Map[String, Any] = Map("pid" -> entity.pid,
                                                "parentPid"-> entity.parentPid,
                                                "name" -> entity.name,
                                                "command" -> entity.command,
                                                "owner" -> entity.owner,
                                                "residentBytes" -> entity.residentBytes,
                                                "softwareVersion" -> entity.softwareVersion)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)


      val node2: Option[Node] = entity.software.toGraph(entity.software)


      val relationship = "HAS_software"
      GraphDBExecutor.setGraphRelationship(node,node2,relationship)

      node

    }

    override def fromGraph(nodeId: Long): Option[ProcessInfo] = {

      val listOfKeys: List[String] = List("pid", "parentPid", "name", "command", "owner", "residentBytes", "softwareVersion")


      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val pid: Int = propertyValues.get("pid").get.toString.toInt
      val parentPid: Int = propertyValues.get("parentPid").get.toString.toInt
      val name: String = propertyValues.get("name").get.toString
      val command: String = propertyValues.get("command").get.toString
      val owner: String = propertyValues.get("owner").get.toString
      val residentBytes: Long = propertyValues.get("residentBytes").get.toString.toLong
      val softwareVersion: String = propertyValues.get("softwareVersion").get.toString


      val relationship = "HAS_software"
      val childNodeId = GraphDBExecutor.getChildNodeId(nodeId,relationship)

      val soft:Software = null
      val software: Option[Software] = soft.fromGraph(childNodeId)
      Some(ProcessInfo(pid, parentPid, name, command, owner, residentBytes, software.get, softwareVersion))

    }
  }

  implicit class KeyPairInfoImpl(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {

    val label = "KeyPairInfo"

    override def toGraph(entity: KeyPairInfo): Option[Node] = {

      val map: Map[String, Any] = Map(
                                    "keyName" -> entity.keyName,
                                    "keyFingerprint" -> entity.keyFingerprint,
                                    "keyMaterial" -> entity.keyMaterial,
                                    "filePath" -> entity.filePath,
                                    "status" -> entity.status.toString,
                                    "defaultUser" -> entity.defaultUser,
                                    "passPhrase" -> entity.passPhrase)

      val node = GraphDBExecutor.createEmptyGraphNode[KeyPairInfo](label, map)

      node
    }

    override def fromGraph(nodeId: Long): Option[KeyPairInfo] = {

      val listOfKeys: List[String] = List("keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)

      val keyName: String = propertyValues.get("keyName").get.toString
      val keyFingerprint: String = propertyValues.get("keyFingerprint").get.toString
      val keyMaterial: String = propertyValues.get("keyMaterial").get.toString
      val filePath: String = propertyValues.get("filePath").get.toString
      val status: KeyPairStatus =  KeyPairStatus.withName(propertyValues.get("status").get.asInstanceOf[String])
      val defaultUser: String = propertyValues.get("defaultUser").get.toString
      val passPhrase: String = propertyValues.get("passPhrase").get.toString


      Some(KeyPairInfo(keyName,keyFingerprint,keyMaterial, filePath, status, defaultUser, passPhrase))
    }

  }

  implicit class SSHAccessInfoImpl(sshAccessInfo: SSHAccessInfo) extends Neo4jRep[SSHAccessInfo] {
    override def toGraph(entity: SSHAccessInfo): Option[Node] = {


      val label: String = "SSHAccessInfo"

      val mapPrimitives : Map[String, Any] = Map("userName" -> entity.userName, "port" -> entity.port)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[SSHAccessInfo](label, mapPrimitives)


      val node2: Option[Node] = entity.keyPair.toGraph(entity.keyPair)


      val relationship = "HAS_keyPair"
      GraphDBExecutor.setGraphRelationship(node,node2,relationship)

      node

    }

    override def fromGraph(nodeId: Long): Option[SSHAccessInfo] = {

      val listOfKeys: List[String] = List("userName","port")


      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val userName: String = propertyValues.get("userName").get.toString
      val port: Int = propertyValues.get("port").get.toString.toInt


      val relationship = "HAS_keyPair"
      val childNodeId = GraphDBExecutor.getChildNodeId(nodeId,relationship)

      val keyPair:KeyPairInfo = null
      val keyPairInfo: Option[KeyPairInfo] = keyPair.fromGraph(childNodeId)
      Some(SSHAccessInfo(keyPairInfo.get, userName, port))

    }
  }

  implicit class InstanceImpl(instance: Instance) extends Neo4jRep[Instance]{

    override def toGraph(entity: Instance): Option[Node] = {

      val label: String = "Instance"

      val mapPrimitives : Map[String, Any] = Map("instanceId" -> entity.instanceId,
                                                "name"        -> entity.name,
                                                "state"       -> entity.state,
                                                "platform"      -> entity.platform,
                                                "architecture"  -> entity.architecture,
                                                "publicDnsName" -> entity.publicDnsName
                                                )

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

//TO BE DONE
/*    val dateNode: Option[Node] = entity.launchTime.toGraph(entity.launchTime)
      val relationship_date = "HAS_date"
      GraphDBExecutor.setGraphRelationship(node,dateNode,relationship_date)
*/

      val memoryInfoNode: Option[Node] = entity.memoryInfo.toGraph(entity.memoryInfo)
      val relationship_storage1 = "HAS_storageInfo1"
      GraphDBExecutor.setGraphRelationship(node,memoryInfoNode,relationship_storage1)

      val rootDiskInfoNode: Option[Node] = entity.rootDiskInfo.toGraph(entity.rootDiskInfo)
      val relationship_storage2 = "HAS_storageInfo2"
      GraphDBExecutor.setGraphRelationship(node,rootDiskInfoNode,relationship_storage2)

      val sshAccessInfoNode: Option[Node] = entity.sshAccessInfo.toGraph(entity.sshAccessInfo)
      val relationship_ssh = "HAS_sshAccessInfo"
      GraphDBExecutor.setGraphRelationship(node,sshAccessInfoNode,relationship_ssh)

      val imageInfoNode: Option[Node] = entity.image.toGraph(entity.image)
      val relationship_image = "HAS_imageInfo"
      GraphDBExecutor.setGraphRelationship(node,imageInfoNode,relationship_image)





      val relationship_tuple = "HAS_tuple"
      entity.tags.foreach{tag =>

        val tagNode = tag.toGraph(tag)
        GraphDBExecutor.setGraphRelationship(node,tagNode,relationship_tuple)

      }

      val relationship_inst1 = "HAS_instanceConnection1"
      entity.liveConnections.foreach{liveConnection =>

        val liveConnectionNode = liveConnection.toGraph(liveConnection)
        GraphDBExecutor.setGraphRelationship(node,liveConnectionNode,relationship_inst1)

      }

      val relationship_inst2 = "HAS_instanceConnection2"
      entity.estimatedConnections.foreach{estimatedConnection =>

        val estimatedConnectionNode = estimatedConnection.toGraph(estimatedConnection)
        GraphDBExecutor.setGraphRelationship(node,estimatedConnectionNode,relationship_inst2)

      }

      val relationship_instuser = "HAS_instanceUser"
      entity.existingUsers.foreach{existingUser =>

        val existingUserNode = existingUser.toGraph(existingUser)
        GraphDBExecutor.setGraphRelationship(node,existingUserNode,relationship_instuser)

      }

      val relationship_process = "HAS_processInfo"
      entity.processes.foreach{process=>

        val processNode = process.toGraph(process)
        GraphDBExecutor.setGraphRelationship(node,processNode,relationship_process)

      }


      node
    }

    override def fromGraph(nodeId: Long): Option[Instance] = {

      val listOfKeys: List[String] = List("instanceId","name","state","platform", "architecture", "publicDnsName")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val instanceId: String = propertyValues.get("instanceId").get.toString
      val name: String = propertyValues.get("name").get.toString
      val state: String = propertyValues.get("state").get.toString
      val platform: String = propertyValues.get("platform").get.toString
      val architecture: String = propertyValues.get("architecture").get.toString
      val publicDnsName: String = propertyValues.get("publicDnsName").get.toString

      val relationship_info1 = "HAS_storageInfo1"
      val childNodeId_info1 = GraphDBExecutor.getChildNodeId(nodeId,relationship_info1)

      val storageInfo: StorageInfo = null
      val memoryInfo: Option[StorageInfo] = storageInfo.fromGraph(childNodeId_info1)

      val relationship_info2 = "HAS_storageInfo2"
      val childNodeId_info2 = GraphDBExecutor.getChildNodeId(nodeId,relationship_info2)

//      val storageInfo: StorageInfo = null
      val rootDiskInfo: Option[StorageInfo] = storageInfo.fromGraph(childNodeId_info2)

      val relationship_ssh = "HAS_sshAccessInfo"
      val childNodeId_ssh = GraphDBExecutor.getChildNodeId(nodeId,relationship_ssh)

      val ssh: SSHAccessInfo = null
      val sshAccessInfo: Option[SSHAccessInfo] = ssh.fromGraph(childNodeId_ssh)

      val relationship_image = "HAS_image"
      val childNodeId_image = GraphDBExecutor.getChildNodeId(nodeId,relationship_image)

      val image:ImageInfo = null
      val imageInfo: Option[ImageInfo] = image.fromGraph(childNodeId_image)




      val relationship_tuple = "HAS_tuple"
      val childNodeIds_tuple: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_tuple)

      val tags: List[Tuple] = childNodeIds_tuple.map{ childId =>
                                                      val tuple:Tuple = null
                                                      tuple.fromGraph(childId).get
                                                    }

      val relationship_inst1 = "HAS_instanceConnection1"
      val childNodeIds_inst1: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_inst1)

      val liveConnections: List[InstanceConnection] = childNodeIds_inst1.map{ childId =>
        val inst:InstanceConnection = null
        inst.fromGraph(childId).get
      }

      val relationship_inst2 = "HAS_instanceConnection2"
      val childNodeIds_inst2: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_inst2)

      val estimatedConnections: List[InstanceConnection] = childNodeIds_inst2.map{ childId =>
        val inst:InstanceConnection = null
        inst.fromGraph(childId).get
      }

      val relationship_user = "HAS_instanceUser"
      val childNodeIds_user: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_user)

      val existingUsers: List[InstanceUser] = childNodeIds_user.map{ childId =>
        val user:InstanceUser = null
        user.fromGraph(childId).get
      }

      val relationship_process = "HAS_processInfo"
      val childNodeIds_process: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship_process)

      val processes: Set[ProcessInfo] = childNodeIds_process.map{ childId =>
        val info:ProcessInfo = null
        info.fromGraph(childId).get
      }.toSet

      val launchTime: Date = null

      Some(Instance(instanceId, name, state, platform, architecture, publicDnsName, launchTime, memoryInfo.get, rootDiskInfo.get,
        tags, sshAccessInfo.get,  liveConnections, estimatedConnections, processes,imageInfo.get, existingUsers))
    }

  }


}