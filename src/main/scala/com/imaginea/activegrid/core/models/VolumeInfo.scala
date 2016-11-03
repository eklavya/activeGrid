package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

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
  val repository = Neo4jRepository
  val volumeInfoLabel = "VolumeInfo"
  val volumeInfo_Tag_Relation = "HAS_TAGS"
  val volumeInfo_SnapshotInfo_Relation = "HAS_SNAPSHOT"

  implicit class VolumeInfoImpl(volumeInfo: VolumeInfo) extends Neo4jRep[VolumeInfo] {
    override def toNeo4jGraph(entity: VolumeInfo): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(volumeInfoLabel)(neo)
          node.setProperty("volumeId", entity.volumeId)
          node.setProperty("size", entity.size)
          node.setProperty("snapshotId", entity.snapshotId)
          node.setProperty("availabilityZone", entity.availabilityZone)
          node.setProperty("state", entity.state)
          node.setProperty("createTime", entity.createTime)
          node.setProperty("volumeType", entity.volumeType)
          entity.tags.foreach {
            tag =>
              val tagNode = tag.toNeo4jGraph(tag)
              repository.createRelation(volumeInfo_Tag_Relation, node, tagNode)
          }
          if (entity.currentSnapshot.nonEmpty) {
            val snapshotInfoNode = entity.currentSnapshot.get.toNeo4jGraph(entity.currentSnapshot.get)
            repository.createRelation(volumeInfo_SnapshotInfo_Relation, node, snapshotInfoNode)
          }

          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[VolumeInfo] = {
      VolumeInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[VolumeInfo] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        if (repository.hasLabel(node, volumeInfoLabel)) {
          val tupleObj = node.getRelationships.foldLeft(Tuple2[List[AnyRef], AnyRef](List.empty[AnyRef], AnyRef)) {
            (tuple, relationship) =>
              val childNode = relationship.getEndNode
              relationship.getType.name match {
                case `volumeInfo_Tag_Relation` => Tuple2(tuple._1.::(KeyValueInfo.fromNeo4jGraph(childNode.getId)), tuple._2)
                case `volumeInfo_SnapshotInfo_Relation` => Tuple2(tuple._1, SnapshotInfo.fromNeo4jGraph(childNode.getId))
              }
          }
          Some(VolumeInfo(Some(nodeId),
            repository.getProperty[String](node, "volumeId").get,
            repository.getProperty[Int](node, "size").get,
            repository.getProperty[String](node, "snapshotId").get,
            repository.getProperty[String](node, "availabilityZone").get,
            repository.getProperty[String](node, "state").get,
            repository.getProperty[String](node, "createTime").get,
            tupleObj._1.asInstanceOf[List[KeyValueInfo]],
            repository.getProperty[String](node, "volumeType").get,
            repository.getProperty[Int](node, "snapshotCount").get,
            Some(tupleObj._2.asInstanceOf[SnapshotInfo])))
        } else {
          None
        }
    }
  }
}
