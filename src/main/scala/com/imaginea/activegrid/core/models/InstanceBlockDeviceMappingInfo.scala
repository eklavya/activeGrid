package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.{Node, NotFoundException}
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
  val ibdAndVolumeInfoRelation = "HAS_VOLUME_INFO"
  val logger = LoggerFactory.getLogger(getClass)

  def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode.flatMap {
      node =>
        if (Neo4jRepository.hasLabel(node, instanceBlockDeviceMappingInfoLabel)) {
          val mayBeVolumeInfo = Neo4jRepository.getChildNodeId(nodeId, ibdAndVolumeInfoRelation).flatMap(id => VolumeInfo.fromNeo4jGraph(id))
          val volumeInfo = mayBeVolumeInfo match {
            case Some(volume) => volume
            case None =>
              logger.warn(s"Volume info is not found with relation $ibdAndVolumeInfoRelation")
              throw new NotFoundException(s"Volume info is not found with relation $ibdAndVolumeInfoRelation")
          }
          val map = Neo4jRepository.getProperties(node, "deviceName", "status",
            "attachTime", "deleteOnTermination", "usage")

          Some(InstanceBlockDeviceMappingInfo(
            Some(nodeId),
            map("deviceName").asInstanceOf[String],
            volumeInfo,
            map("status").asInstanceOf[String],
            map("attachTime").asInstanceOf[String],
            map("deleteOnTermination").asInstanceOf[Boolean],
            map("usage").asInstanceOf[Int]))
        } else {
          None
        }
    }
  }

  implicit class InstanceBlockDeviceMappingInfoImpl(instanceBlockDeviceMappingInfo: InstanceBlockDeviceMappingInfo)
    extends Neo4jRep[InstanceBlockDeviceMappingInfo] {
    override def toNeo4jGraph(entity: InstanceBlockDeviceMappingInfo): Node = {
      val map = Map("deviceName" -> entity.deviceName,
        "status" -> entity.status,
        "attachTime" -> entity.attachTime,
        "deleteOnTermination" -> entity.deleteOnTermination,
        "usage" -> entity.usage)
      val node = Neo4jRepository.saveEntity[InstanceBlockDeviceMappingInfo](instanceBlockDeviceMappingInfoLabel, entity.id, map)
      val childNode = entity.volume.toNeo4jGraph(entity.volume)
      Neo4jRepository.createRelation(ibdAndVolumeInfoRelation, node, childNode)
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
      InstanceBlockDeviceMappingInfo.fromNeo4jGraph(nodeId)
    }
  }

}
