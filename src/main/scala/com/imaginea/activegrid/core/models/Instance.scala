package com.imaginea.activegrid.core.models

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
                    tags: List[KeyValueInfo],
                    sshAccessInfo: Option[SSHAccessInfo],
                    liveConnections: List[InstanceConnection],
                    estimatedConnections: List[InstanceConnection],
                    processes: Set[ProcessInfo],
                    image: Option[ImageInfo],
                    existingUsers: List[InstanceUser],
                    account: Option[AccountInfo],
                    availabilityZone: Option[String],
                    privateDnsName: Option[String],
                    privateIpAddress: Option[String],
                    publicIpAddress: Option[String],
                    elasticIP: Option[String],
                    monitoring: Option[String],
                    rootDeviceType: Option[String],
                    blockDeviceMappings: List[InstanceBlockDeviceMappingInfo],
                    securityGroups: List[SecurityGroupInfo],
                    reservedInstance: Boolean,
                    region: Option[String]
                   ) extends BaseEntity

object Instance {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  // scalastyle:off
  def apply(instanceId: Option[String], name: String, state: Option[String],
            instanceType: Option[String], platform: Option[String], architecture: Option[String],
            publicDnsName: Option[String], launchTime: Option[Long], memoryInfo: Option[StorageInfo],
            rootDiskInfo: Option[StorageInfo], tags: List[KeyValueInfo], imageInfo: Option[ImageInfo],
            sshAccessInfo: Option[SSHAccessInfo]): Instance =
    Instance(None, instanceId, name, state, instanceType, platform,
      architecture, publicDnsName, launchTime, memoryInfo, rootDiskInfo,
      tags, sshAccessInfo, List.empty[InstanceConnection], List.empty[InstanceConnection],
      Set.empty[ProcessInfo], imageInfo, List.empty[InstanceUser], None, None, None, None, None,
      None, None, None, List.empty, List.empty, reservedInstance = false, None)


  def apply(name: String, tags: List[KeyValueInfo], processes: Set[ProcessInfo]): Instance =
    Instance(None, None, name, None, None, None, None, None, None, None, None, tags,
      None, List.empty[InstanceConnection], List.empty[InstanceConnection], processes, None,
      List.empty[InstanceUser], None, None, None, None, None, None, None, None, List.empty, List.empty,
      reservedInstance = false, None)

  def apply(name: String): Instance =
    Instance(None, None, name, None, None, None, None, None, None, None,
      None, List.empty[KeyValueInfo], None, List.empty[InstanceConnection],
      List.empty[InstanceConnection], Set.empty[ProcessInfo], None,
      List.empty[InstanceUser], None, None, None, None, None, None,
      None, None, List.empty, List.empty, reservedInstance = false, None)

  def fromNeo4jGraph(nodeId: Long): Option[Instance] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "instanceId", "name", "state", "instanceType",
          "platform", "architecture", "publicDnsName", "availabilityZone", "privateDnsName", "privateIpAddress",
          "publicIpAddress", "elasticIP", "monitoring", "rootDeviceType", "reservedInstance", "region")
        val instanceId = map.get("instanceId").asInstanceOf[Option[String]]
        val name = map("name").toString
        val state = map.get("state").asInstanceOf[Option[String]]
        val instanceType = map.get("instanceType").asInstanceOf[Option[String]]
        val platform = map.get("platform").asInstanceOf[Option[String]]
        val architecture = map.get("architecture").asInstanceOf[Option[String]]
        val publicDnsName = map.get("publicDnsName").asInstanceOf[Option[String]]
        val availabilityZone = map.get("availabilityZone").asInstanceOf[Option[String]]
        val privateDnsName = map.get("privateDnsName").asInstanceOf[Option[String]]
        val privateIpAddress = map.get("privateIpAddress").asInstanceOf[Option[String]]
        val publicIpAddress = map.get("publicIpAddress").asInstanceOf[Option[String]]
        val elasticIP = map.get("elasticIP").asInstanceOf[Option[String]]
        val monitoring = map.get("monitoring").asInstanceOf[Option[String]]
        val rootDeviceType = map.get("rootDeviceType").asInstanceOf[Option[String]]
        val reservedInstance = if (map.get("reservedInstance").nonEmpty) map("reservedInstance").asInstanceOf[Boolean] else false
        val region = map.get("region").asInstanceOf[Option[String]]
        //TO DO
        //val launchTime: Date = new Date(map.get("launchTime").get.toString.toLong)
        val launchTime: Option[Long] = Some(100)
        //map.get("launchTime").get.toString.toLong

        val relationship_info1 = "HAS_storageInfo1"
        val memoryInfo: Option[StorageInfo] = Neo4jRepository.getChildNodeId(nodeId, relationship_info1).flatMap(id => StorageInfo.fromNeo4jGraph(id))

        val relationship_info2 = "HAS_storageInfo2"
        val rootDiskInfo: Option[StorageInfo] = Neo4jRepository.getChildNodeId(nodeId, relationship_info2).flatMap(id => StorageInfo.fromNeo4jGraph(id))

        val relationship_ssh = "HAS_sshAccessInfo"
        val sshAccessInfo: Option[SSHAccessInfo] = Neo4jRepository.getChildNodeId(nodeId, relationship_ssh).flatMap(id => SSHAccessInfo.fromNeo4jGraph(id))

        val relationship_image = "HAS_imageInfo"
        val imageInfo: Option[ImageInfo] = Neo4jRepository.getChildNodeId(nodeId, relationship_image).flatMap(id => ImageInfo.fromNeo4jGraph(id))

        val relationship_keyValueInfo = "HAS_keyValueInfo"
        val childNodeIds_keyVal: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_keyValueInfo)
        val tags: List[KeyValueInfo] = childNodeIds_keyVal.flatMap { childId =>
          KeyValueInfo.fromNeo4jGraph(childId)
        }

        val relationship_inst1 = "HAS_instanceConnection1"
        val childNodeIds_inst1: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_inst1)
        val liveConnections: List[InstanceConnection] = childNodeIds_inst1.flatMap { childId =>
          InstanceConnection.fromNeo4jGraph(childId)
        }

        val relationship_inst2 = "HAS_instanceConnection2"
        val childNodeIds_inst2: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_inst2)
        val estimatedConnections: List[InstanceConnection] = childNodeIds_inst2.flatMap { childId =>
          InstanceConnection.fromNeo4jGraph(childId)
        }

        val relationship_user = "HAS_instanceUser"
        val childNodeIds_user: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_user)
        val existingUsers: List[InstanceUser] = childNodeIds_user.flatMap { childId =>
          InstanceUser.fromNeo4jGraph(childId)
        }

        val relationship_process = "HAS_processInfo"
        val childNodeIds_process: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_process)
        val processes: Set[ProcessInfo] = childNodeIds_process.flatMap { childId =>
          ProcessInfo.fromNeo4jGraph(childId)
        }.toSet

        val relationship_blockDevice = "HAS_blockDeviceMapping"
        val childNodeIds_blockDevice = Neo4jRepository.getChildNodeIds(nodeId, relationship_blockDevice)
        val blockDeviceMappings: List[InstanceBlockDeviceMappingInfo] = childNodeIds_blockDevice.flatMap { childId =>
          InstanceBlockDeviceMappingInfo.fromNeo4jGraph(childId)
        }

        val relationship_securityGroup = "HAS_securityGroup"
        val childNodeIds_securityGroup = Neo4jRepository.getChildNodeIds(nodeId, relationship_securityGroup)
        val securityGroups: List[SecurityGroupInfo] = childNodeIds_securityGroup.flatMap { childId =>
          SecurityGroupInfo.fromNeo4jGraph(childId)
        }

        Some(Instance(Some(nodeId), instanceId, name, state, instanceType, platform, architecture, publicDnsName,
          launchTime, memoryInfo, rootDiskInfo,
          tags, sshAccessInfo, liveConnections, estimatedConnections, processes, imageInfo, existingUsers,
          None, availabilityZone, privateDnsName, privateIpAddress, publicIpAddress, elasticIP, monitoring,
          rootDeviceType, blockDeviceMappings, securityGroups, reservedInstance, region))
      case None =>
        logger.warn(s"could not find node for Instance with nodeId $nodeId")
        None
    }

  }

  implicit class InstanceImpl(instance: Instance) extends Neo4jRep[Instance] {

    override def toNeo4jGraph(entity: Instance): Node = {
      logger.info(s"Executing $getClass :: toNeo4jGraph")
      val label = "Instance"
      val mapPrimitives = Map("instanceId" -> entity.instanceId,
        "name" -> entity.name, "state" -> entity.state,
        "instanceType" -> entity.instanceType, "platform" -> entity.platform,
        "architecture" -> entity.architecture, "publicDnsName" -> entity.publicDnsName,
        "launchTime" -> entity.launchTime, "availabilityZone" -> entity.availabilityZone,
        "privateDnsName" -> entity.privateDnsName, "privateIpAddress" -> entity.privateIpAddress,
        "publicIpAddress" -> entity.publicIpAddress, "elasticIP" -> entity.elasticIP,
        "monitoring" -> entity.monitoring, "rootDeviceType" -> entity.rootDeviceType,
        "reservedInstance" -> entity.reservedInstance, "region" -> entity.region
      )
      //logger.info(s"Printging Instance : $entity  : ID : ${entity.id.get.getClass}")
      val node = Neo4jRepository.saveEntity[Instance](label, entity.id, mapPrimitives)

      entity.memoryInfo match {
        case Some(mInfo) =>
          val memoryInfoNode = mInfo.toNeo4jGraph(mInfo)
          val relationship_storage1 = "HAS_storageInfo1"
          Neo4jRepository.setGraphRelationship(node, memoryInfoNode, relationship_storage1)
        case None => logger.debug("entity Instance has no memoryInfo")
      }

      entity.rootDiskInfo match {
        case Some(rInfo) =>
          val rootDiskInfoNode = rInfo.toNeo4jGraph(rInfo)
          val relationship_storage2 = "HAS_storageInfo2"
          Neo4jRepository.setGraphRelationship(node, rootDiskInfoNode, relationship_storage2)
        case None => logger.info("entity Instance has no rootDiskInfo")
      }

      entity.sshAccessInfo match {
        case Some(ssh) =>
          val sshAccessInfoNode = ssh.toNeo4jGraph(ssh)
          val relationship_ssh = "HAS_sshAccessInfo"
          Neo4jRepository.setGraphRelationship(node, sshAccessInfoNode, relationship_ssh)
        case None => logger.info("entity Instance has no sshAccessInfo")
      }

      entity.image match {
        case Some(image) =>
          val imageInfoNode = image.toNeo4jGraph(image)
          val relationship_image = "HAS_imageInfo"
          Neo4jRepository.setGraphRelationship(node, imageInfoNode, relationship_image)
        case None => logger.info("entity Instance has no imageInfo")
      }

      val relationship_keyVal = "HAS_keyValueInfo"
      entity.tags.foreach { tag =>
        val tagNode = tag.toNeo4jGraph(tag)
        Neo4jRepository.setGraphRelationship(node, tagNode, relationship_keyVal)
      }

      val relationship_inst1 = "HAS_instanceConnection1"
      entity.liveConnections.foreach { liveConnection =>
        val liveConnectionNode = liveConnection.toNeo4jGraph(liveConnection)
        Neo4jRepository.setGraphRelationship(node, liveConnectionNode, relationship_inst1)
      }

      val relationship_inst2 = "HAS_instanceConnection2"
      entity.estimatedConnections.foreach { estimatedConnection =>
        val estimatedConnectionNode = estimatedConnection.toNeo4jGraph(estimatedConnection)
        Neo4jRepository.setGraphRelationship(node, estimatedConnectionNode, relationship_inst2)
      }

      val relationship_instuser = "HAS_instanceUser"
      entity.existingUsers.foreach { existingUser =>
        val existingUserNode = existingUser.toNeo4jGraph(existingUser)
        Neo4jRepository.setGraphRelationship(node, existingUserNode, relationship_instuser)
      }

      val relationship_process = "HAS_processInfo"
      entity.processes.foreach { process =>
        val processNode = process.toNeo4jGraph(process)
        Neo4jRepository.setGraphRelationship(node, processNode, relationship_process)
      }

      val relationship_blockingDeviceMapping = "HAS_blockDeviceMapping"
      entity.blockDeviceMappings.foreach { blockDeviceMapping =>
        val blockDeviceNode = blockDeviceMapping.toNeo4jGraph(blockDeviceMapping)
        Neo4jRepository.setGraphRelationship(node, blockDeviceNode, relationship_blockingDeviceMapping)
      }

      val relationship_securityGroup = "HAS_securityGroup"
      entity.securityGroups.foreach { securityGroup =>
        val securityGroupNode = securityGroup.toNeo4jGraph(securityGroup)
        Neo4jRepository.setGraphRelationship(node, securityGroupNode, relationship_securityGroup)
      }
      node
    }
    // scalastyle:on
    override def fromNeo4jGraph(id: Long): Option[Instance] = {
      Instance.fromNeo4jGraph(id)
    }
  }

}
