package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class VolumeInfo(override val id: Option[Long],
                      volumeId: Option[String],
                      size: Option[Int],
                      snapshotId: Option[String],
                      availabilityZone: Option[String],
                      state: Option[String],
                      createTime: Option[String],
                      tags: List[KeyValueInfo],
                      volumeType: Option[String],
                      snapshotCount: Option[Int],
                      currentSnapshot: Option[SnapshotInfo]) extends BaseEntity

object VolumeInfo {
  val volumeInfoLabel = "VolumeInfo"
  val volumeInfo_Tag_Relation = "HAS_TAGS"
  val volumeInfo_SnapshotInfo_Relation = "HAS_SNAPSHOT"
  val logger = LoggerFactory.getLogger(getClass)

  def apply(id: Long): VolumeInfo = {
    VolumeInfo(Some(id), None, None, None, None, None, None, List.empty[KeyValueInfo], None, None, None)
  }

  implicit class VolumeInfoImpl(volumeInfo: VolumeInfo) extends Neo4jRep[VolumeInfo] {
    override def toNeo4jGraph(entity: VolumeInfo): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph ")
      val map = Map("volumeId" -> entity.volumeId,
        "size" -> entity.size,
        "snapshotId" -> entity.snapshotId,
        "availabilityZone" -> entity.availabilityZone,
        "state" -> entity.state,
        "createTime" -> entity.createTime,
        "volumeType" -> entity.volumeType,
        "snapshotCount" -> entity.snapshotCount
      )
      val node = Neo4jRepository.saveEntity[VolumeInfo](volumeInfoLabel, entity.id, map)
      entity.tags.foreach {
        tag =>
          val tagNode = tag.toNeo4jGraph(tag)
          Neo4jRepository.createRelation(volumeInfo_Tag_Relation, node, tagNode)
      }
      if (entity.currentSnapshot.nonEmpty) {
        val snapshotInfoNode = entity.currentSnapshot.get.toNeo4jGraph(entity.currentSnapshot.get)
        Neo4jRepository.createRelation(volumeInfo_SnapshotInfo_Relation, node, snapshotInfoNode)
      }

      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[VolumeInfo] = {
      VolumeInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[VolumeInfo] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph")
    val maybeNode = Neo4jRepository.findNodeById(nodeId)
    maybeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, volumeInfoLabel)) {
          val map = Neo4jRepository.getProperties(node, "volumeId", "size", "snapshotId", "availabilityZone", "state", "createTime", "volumeType")

          val childNodeIds_keyValueInfos = Neo4jRepository.getChildNodeIds(nodeId, volumeInfo_Tag_Relation)
          val keyValueInfos: List[KeyValueInfo] = childNodeIds_keyValueInfos.flatMap { childId =>
            KeyValueInfo.fromNeo4jGraph(childId)
          }

          val childNodeIds_snapshots = Neo4jRepository.getChildNodeIds(nodeId, volumeInfo_SnapshotInfo_Relation)
          val snapshotInfo: List[SnapshotInfo] = childNodeIds_snapshots.flatMap { childId =>
            SnapshotInfo.fromNeo4jGraph(childId)
          }
          Some(VolumeInfo(Some(nodeId),
            ActiveGridUtils.getValueFromMapAs[String](map, "volumeId"),
            ActiveGridUtils.getValueFromMapAs[Int](map, "size"),
            ActiveGridUtils.getValueFromMapAs[String](map, "snapshotId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "availabilityZone"),
            ActiveGridUtils.getValueFromMapAs[String](map, "state"),
            ActiveGridUtils.getValueFromMapAs[String](map, "createTime"),
            keyValueInfos,
            ActiveGridUtils.getValueFromMapAs[String](map, "volumeType"),
            ActiveGridUtils.getValueFromMapAs[Int](map, "snapshotCount"),
            snapshotInfo.headOption))

        } else {
          None
        }
      case None => None
    }
  }
}
