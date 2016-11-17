package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class InstanceBlockDeviceMappingInfo(override val id: Option[Long],
                                          deviceName: String,
                                          volume: VolumeInfo,
                                          status: String,
                                          attachTime: String,
                                          deleteOnTermination: Boolean,
                                          usage: Int) extends BaseEntity

object InstanceBlockDeviceMappingInfo {
  val instanceBlockDeviceMappingInfoLabel = "InstanceBlockDeviceMappingInfo"
  val ibd_VolumeInfo_Relation = "HAS_VOLUME_INFO"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class InstanceBlockDeviceMappingInfoImpl(instanceBlockDeviceMappingInfo: InstanceBlockDeviceMappingInfo) extends Neo4jRep[InstanceBlockDeviceMappingInfo] {
    override def toNeo4jGraph(entity: InstanceBlockDeviceMappingInfo): Node = {
      val map = Map("deviceName" -> entity.deviceName,
        "status" -> entity.status,
        "attachTime" -> entity.attachTime,
        "deleteOnTermination" -> entity.deleteOnTermination,
        "usage" -> entity.usage)
      val node = Neo4jRepository.saveEntity[InstanceBlockDeviceMappingInfo](instanceBlockDeviceMappingInfoLabel, entity.id, map)
      val childNode = entity.volume.toNeo4jGraph(entity.volume)
      Neo4jRepository.createRelation(ibd_VolumeInfo_Relation, node, childNode)
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
      InstanceBlockDeviceMappingInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph with ID $nodeId")
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, instanceBlockDeviceMappingInfoLabel)) {
          val childNodeIds_volumeInfo: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, ibd_VolumeInfo_Relation)
          val volumeInfos: List[VolumeInfo] = childNodeIds_volumeInfo.flatMap { childId =>
            VolumeInfo.fromNeo4jGraph(childId)
          }
          val map = Neo4jRepository.getProperties(node, "deviceName", "status", "attachTime", "deleteOnTermination", "usage")
          //logger.info(s"getting : $volumeInfos")
          Some(InstanceBlockDeviceMappingInfo(
            Some(nodeId),
            map("deviceName").asInstanceOf[String],
            volumeInfos.head,
            map("status").asInstanceOf[String],
            map("attachTime").asInstanceOf[String],
            map("deleteOnTermination").asInstanceOf[Boolean],
            map("usage").asInstanceOf[Int]))
        } else {
          None
        }
      case None => None
    }
  }
}
