package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class VolumeInfo(override val id: Option[Long],
                      volumeId: String,
                      size: Int,
                      snapshotId: String,
                      availabilityZone: String,
                      state: String,
                      createTime: String,
                      tags: List[KeyValueInfo],
                      volumeType: String,
                      snapshotCount: Int,
                      currentSnapshot: Option[SnapshotInfo]) extends BaseEntity

object VolumeInfo {
  val volumeInfoLabel = "VolumeInfo"
  val volumeInfo_Tag_Relation = "HAS_TAGS"
  val volumeInfo_SnapshotInfo_Relation = "HAS_SNAPSHOT"
  val logger = LoggerFactory.getLogger(getClass)

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
          val tupleObj = node.getRelationships.foldLeft(Tuple2[List[AnyRef], AnyRef](List.empty[AnyRef], AnyRef)) {
            (tuple, relationship) =>
              val childNode = relationship.getEndNode
              relationship.getType.name match {
                case `volumeInfo_Tag_Relation` => (KeyValueInfo.fromNeo4jGraph(childNode.getId) :: tuple._1, tuple._2)
                case `volumeInfo_SnapshotInfo_Relation` => (tuple._1, SnapshotInfo.fromNeo4jGraph(childNode.getId))
              }
          }
          Some(VolumeInfo(Some(nodeId),
            map("volumeId").toString,
            map("size").asInstanceOf[Int],
            map("snapshotId").toString,
            map("availabilityZone").toString,
            map("state").toString,
            map("createTime").toString,
            tupleObj._1.asInstanceOf[List[KeyValueInfo]],
            map("volumeType").toString,
            map("snapshotCount").asInstanceOf[Int],
            Some(tupleObj._2.asInstanceOf[SnapshotInfo])))
        } else {
          None
        }
      case None => None
    }
  }
}
