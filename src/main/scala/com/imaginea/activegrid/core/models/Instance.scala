package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
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
  val LaunchTime = 100

  val label = Instance.getClass.getName
  val relationLabel = ActiveGridUtils.relationLbl(label)

  def apply(instanceId: Option[String], name: String, state: Option[String], instanceType: Option[String],
            platform: Option[String], architecture: Option[String], publicDnsName: Option[String], launchTime: Option[Long],
            memoryInfo: Option[StorageInfo], rootDiskInfo: Option[StorageInfo], tags: List[KeyValueInfo],
            imageInfo: Option[ImageInfo], sshAccessInfo: Option[SSHAccessInfo]): Instance =
    Instance(None, instanceId, name, state, instanceType, platform, architecture, publicDnsName,
      launchTime, memoryInfo, rootDiskInfo, tags, sshAccessInfo, List.empty[InstanceConnection],
      List.empty[InstanceConnection], Set.empty[ProcessInfo], imageInfo, List.empty[InstanceUser],
      None, None, None, None, None, None, None, None, List.empty, List.empty, reservedInstance = false, None
    )

  //scalastyle:on

  def apply(name: String, tags: List[KeyValueInfo], processes: Set[ProcessInfo]): Instance =
    Instance(None, None, name, None, None, None, None, None, None, None, None, tags, None,
      List.empty[InstanceConnection], List.empty[InstanceConnection], processes, None,
      List.empty[InstanceUser], None, None, None, None, None, None, None, None, List.empty,
      List.empty, reservedInstance = false, None
    )

  def apply(name: String): Instance =
    Instance(None, None, name, None, None, None, None, None, None, None, None, List.empty[KeyValueInfo],
      None, List.empty[InstanceConnection], List.empty[InstanceConnection], Set.empty[ProcessInfo], None,
      List.empty[InstanceUser], None, None, None, None, None, None, None, None, List.empty, List.empty,
      reservedInstance = false, None
    )

  //scalastyle:off method.length
  def fromNeo4jGraph(nodeId: Long): Option[Instance] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "instanceId", "name", "state", "instanceType", "platform",
          "architecture", "publicDnsName", "availabilityZone", "privateDnsName", "privateIpAddress", "publicIpAddress",
          "elasticIP", "monitoring", "rootDeviceType", "reservedInstance", "region")

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
        val reservedInstance = map.get("reservedInstance") match {
          case Some(value) => value.asInstanceOf[Boolean]
          case _ => false
        }
        val region = map.get("region").asInstanceOf[Option[String]]
        //TO DO
        //val launchTime: Date = new Date(map.get("launchTime").get.toString.toLong)
        val launchTime: Option[Long] = Some(LaunchTime)
        //map.get("launchTime").get.toString.toLong

        val storeageInfoRelation1 = "HAS_storageInfo1"
        val memoryInfo: Option[StorageInfo] =
          Neo4jRepository.getChildNodeId(nodeId, storeageInfoRelation1).flatMap(id => StorageInfo.fromNeo4jGraph(id))

        val storeageInfoRelation2 = "HAS_storageInfo2"
        val rootDiskInfo: Option[StorageInfo] =
          Neo4jRepository.getChildNodeId(nodeId, storeageInfoRelation2).flatMap(id => StorageInfo.fromNeo4jGraph(id))

        val sshRelation = "HAS_sshAccessInfo"
        val sshAccessInfo: Option[SSHAccessInfo] =
          Neo4jRepository.getChildNodeId(nodeId, sshRelation).flatMap(id => SSHAccessInfo.fromNeo4jGraph(id))

        val imageRelation = "HAS_imageInfo"
        val imageInfo: Option[ImageInfo] =
          Neo4jRepository.getChildNodeId(nodeId, imageRelation).flatMap(id => ImageInfo.fromNeo4jGraph(id))

        val keyValueInfoRelation = "HAS_keyValueInfo"
        val childNodeIdsKeyVal: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, keyValueInfoRelation)
        val tags: List[KeyValueInfo] = childNodeIdsKeyVal.flatMap { childId =>
          KeyValueInfo.fromNeo4jGraph(childId)
        }

        val instanceConnectionRelation1 = "HAS_instanceConnection1"
        val childNodeIdsInst1: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, instanceConnectionRelation1)
        val liveConnections: List[InstanceConnection] = childNodeIdsInst1.flatMap { childId =>
          InstanceConnection.fromNeo4jGraph(childId)
        }

        val instanceConnectionRelation2 = "HAS_instanceConnection2"
        val childNodeIdsInst2: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, instanceConnectionRelation2)
        val estimatedConnections: List[InstanceConnection] = childNodeIdsInst2.flatMap { childId =>
          InstanceConnection.fromNeo4jGraph(childId)
        }

        val userRelation = "HAS_instanceUser"
        val childNodeIdsUser: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, userRelation)
        val existingUsers: List[InstanceUser] = childNodeIdsUser.flatMap { childId =>
          InstanceUser.fromNeo4jGraph(childId)
        }

        val processRelation = "HAS_processInfo"
        val childNodeIdsProcess: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, processRelation)
        val processes: Set[ProcessInfo] = childNodeIdsProcess.flatMap { childId =>
          ProcessInfo.fromNeo4jGraph(childId)
        }.toSet

        val blockDeviceRelation = "HAS_blockDeviceMapping"
        val childNodeIdsBlockDevice = Neo4jRepository.getChildNodeIds(nodeId, blockDeviceRelation)
        val blockDeviceMappings: List[InstanceBlockDeviceMappingInfo] =
          childNodeIdsBlockDevice.flatMap { childId => InstanceBlockDeviceMappingInfo.fromNeo4jGraph(childId) }

        val securityGroupRelation = "HAS_securityGroup"
        val childNodeIdsSecurityGroup = Neo4jRepository.getChildNodeIds(nodeId, securityGroupRelation)
        val securityGroups: List[SecurityGroupInfo] = childNodeIdsSecurityGroup.flatMap { childId =>
          SecurityGroupInfo.fromNeo4jGraph(childId)
        }

        Some(Instance(Some(nodeId), instanceId, name, state, instanceType, platform, architecture,
          publicDnsName, launchTime, memoryInfo, rootDiskInfo, tags, sshAccessInfo, liveConnections,
          estimatedConnections, processes, imageInfo, existingUsers, None, availabilityZone, privateDnsName,
          privateIpAddress, publicIpAddress, elasticIP, monitoring, rootDeviceType, blockDeviceMappings,
          securityGroups, reservedInstance, region))
      case None =>
        logger.warn(s"could not find node for Instance with nodeId $nodeId")
        None
    }
    //scalastyle:on method.length
  }

  implicit class InstanceImpl(instance: Instance) extends Neo4jRep[Instance] {
    //scalastyle:off method.length
    override def toNeo4jGraph(entity: Instance): Node = {
      val label = "Instance"
      val mapPrimitives = Map("instanceId" -> entity.instanceId,
        "name" -> entity.name,
        "state" -> entity.state,
        "instanceType" -> entity.instanceType,
        "platform" -> entity.platform,
        "architecture" -> entity.architecture,
        "publicDnsName" -> entity.publicDnsName,
        "launchTime" -> entity.launchTime,
        "availabilityZone" -> entity.availabilityZone,
        "privateDnsName" -> entity.privateDnsName,
        "privateIpAddress" -> entity.privateIpAddress,
        "publicIpAddress" -> entity.publicIpAddress,
        "elasticIP" -> entity.elasticIP,
        "monitoring" -> entity.monitoring,
        "rootDeviceType" -> entity.rootDeviceType,
        "reservedInstance" -> entity.reservedInstance,
        "region" -> entity.region
      )
      val node = Neo4jRepository.saveEntity[Instance](label, entity.id, mapPrimitives)

      entity.memoryInfo match {
        case Some(mInfo) =>
          val memoryInfoNode = mInfo.toNeo4jGraph(mInfo)
          val storageRelation1 = "HAS_storageInfo1"
          Neo4jRepository.setGraphRelationship(node, memoryInfoNode, storageRelation1)
        case None => logger.debug("entity Instance has no memoryInfo")
      }

      entity.rootDiskInfo match {
        case Some(rInfo) =>
          val rootDiskInfoNode = rInfo.toNeo4jGraph(rInfo)
          val storageRelation2 = "HAS_storageInfo2"
          Neo4jRepository.setGraphRelationship(node, rootDiskInfoNode, storageRelation2)
        case None => logger.info("entity Instance has no rootDiskInfo")
      }

      entity.sshAccessInfo match {
        case Some(ssh) =>
          val sshAccessInfoNode = ssh.toNeo4jGraph(ssh)
          val sshRelation = "HAS_sshAccessInfo"
          Neo4jRepository.setGraphRelationship(node, sshAccessInfoNode, sshRelation)
        case None => logger.info("entity Instance has no sshAccessInfo")
      }

      entity.image match {
        case Some(image) =>
          val imageInfoNode = image.toNeo4jGraph(image)
          val imageRelation = "HAS_imageInfo"
          Neo4jRepository.setGraphRelationship(node, imageInfoNode, imageRelation)
        case None => logger.info("entity Instance has no imageInfo")
      }

      val keyValRelation = "HAS_keyValueInfo"
      entity.tags.foreach { tag =>
        val tagNode = tag.toNeo4jGraph(tag)
        Neo4jRepository.setGraphRelationship(node, tagNode, keyValRelation)
      }

      val instanceConnectionRelation1 = "HAS_instanceConnection1"
      entity.liveConnections.foreach { liveConnection =>
        val liveConnectionNode = liveConnection.toNeo4jGraph(liveConnection)
        Neo4jRepository.setGraphRelationship(node, liveConnectionNode, instanceConnectionRelation1)
      }

      val instanceConnectionRelation2 = "HAS_instanceConnection2"
      entity.estimatedConnections.foreach { estimatedConnection =>
        val estimatedConnectionNode = estimatedConnection.toNeo4jGraph(estimatedConnection)
        Neo4jRepository.setGraphRelationship(node, estimatedConnectionNode, instanceConnectionRelation2)
      }

      val instanceUserRelation = "HAS_instanceUser"
      entity.existingUsers.foreach { existingUser =>
        val existingUserNode = existingUser.toNeo4jGraph(existingUser)
        Neo4jRepository.setGraphRelationship(node, existingUserNode, instanceUserRelation)
      }

      val processRelation = "HAS_processInfo"
      entity.processes.foreach { process =>
        val processNode = process.toNeo4jGraph(process)
        Neo4jRepository.setGraphRelationship(node, processNode, processRelation)
      }

      val blockingDeviceMappingRelation = "HAS_blockDeviceMapping"
      entity.blockDeviceMappings.foreach { blockDeviceMapping =>
        val blockDeviceNode = blockDeviceMapping.toNeo4jGraph(blockDeviceMapping)
        Neo4jRepository.setGraphRelationship(node, blockDeviceNode, blockingDeviceMappingRelation)
      }

      val securityGroupRelation = "HAS_securityGroup"
      entity.securityGroups.foreach { securityGroup =>
        val securityGroupNode = securityGroup.toNeo4jGraph(securityGroup)
        Neo4jRepository.setGraphRelationship(node, securityGroupNode, securityGroupRelation)
      }
      node
    }

    //scalastyle:on method.length
    override def fromNeo4jGraph(id: Long): Option[Instance] = {
      Instance.fromNeo4jGraph(id)
    }
  }

  def instanceBySiteAndInstanceID(siteId: Long, instanceId: String): Option[Instance] = {
    Site1.fromNeo4jGraph(siteId).flatMap {
      site => site.applications.flatMap {
        app => app.instances.filter {
          instance => instance.id.getOrElse(0L).toString.equals(instanceId)
        }
      }.headOption
    }
  }
}

