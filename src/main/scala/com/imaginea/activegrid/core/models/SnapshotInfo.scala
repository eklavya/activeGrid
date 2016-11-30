package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class SnapshotInfo(override val id: Option[Long],
                        snapshotId: Option[String],
                        volumeId: Option[String],
                        state: Option[String],
                        startTime: Option[String],
                        progress: Option[String],
                        ownerId: Option[String],
                        ownerAlias: Option[String],
                        description: Option[String],
                        volumeSize: Option[Int],
                        tags: List[KeyValueInfo]) extends BaseEntity

object SnapshotInfo {
  val snapshotInfoLabel = "SnapshotInfo"
  val snapshotInfoAndKeyValueRelation = "HAS_KEY_VALUE"
  val logger = LoggerFactory.getLogger(getClass)

  def appply(id: Long): SnapshotInfo = {
    SnapshotInfo(Some(id), None, None, None, None, None, None, None, None, Some(1), List.empty[KeyValueInfo])
  }

  def fromNeo4jGraph(nodeId: Long): Option[SnapshotInfo] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph")
    val maybeNode = Neo4jRepository.findNodeById(nodeId)
    maybeNode.flatMap {
      node =>
        if (Neo4jRepository.hasLabel(node, snapshotInfoLabel)) {
          val map = Neo4jRepository.getProperties(node, "snapshotId", "volumeId", "state", "startTime", "progress", "ownerId",
            "ownerAlias", "description", "volumeSize")

          val keyValueInfoNodeIds = Neo4jRepository.getChildNodeIds(nodeId, snapshotInfoAndKeyValueRelation)
          val keyValueInfos: List[KeyValueInfo] = keyValueInfoNodeIds.flatMap { childId =>
            KeyValueInfo.fromNeo4jGraph(childId)
          }
          Some(SnapshotInfo(
            Some(nodeId),
            ActiveGridUtils.getValueFromMapAs[String](map, "snapshotId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "volumeId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "state"),
            ActiveGridUtils.getValueFromMapAs[String](map, "startTime"),
            ActiveGridUtils.getValueFromMapAs[String](map, "progress"),
            ActiveGridUtils.getValueFromMapAs[String](map, "ownerId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "ownerAlias"),
            ActiveGridUtils.getValueFromMapAs[String](map, "description"),
            ActiveGridUtils.getValueFromMapAs[Int](map, "volumeSize"),
            keyValueInfos))
        } else {
          None
        }
    }
  }

  implicit class SnapshotInfoImpl(snapshotInfo: SnapshotInfo) extends Neo4jRep[SnapshotInfo] {
    override def toNeo4jGraph(entity: SnapshotInfo): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph")
      val map = Map("snapshotId" -> entity.snapshotId,
        "volumeId" -> entity.volumeId,
        "state" -> entity.state,
        "startTime" -> entity.startTime,
        "progress" -> entity.progress,
        "ownerId" -> entity.ownerId,
        "ownerAlias" -> entity.ownerAlias,
        "description" -> entity.description,
        "volumeSize" -> entity.volumeSize)
      val node = Neo4jRepository.saveEntity[SnapshotInfo](snapshotInfoLabel, entity.id, map)
      entity.tags.foreach {
        tag =>
          val tagNode = tag.toNeo4jGraph(tag)
          Neo4jRepository.createRelation(snapshotInfoAndKeyValueRelation, node, tagNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SnapshotInfo] = {
      SnapshotInfo.fromNeo4jGraph(nodeId)
    }
  }

}
