package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class SnapshotInfo(override val id: Option[Long],
                        snapshotId: String,
                        volumeId: String,
                        state: String,
                        startTime: String,
                        progress: String,
                        ownerId: String,
                        ownerAlias: String,
                        description: String,
                        volumeSize: Int,
                        tags: List[KeyValueInfo]) extends BaseEntity

object SnapshotInfo {
  val repository = Neo4jRepository
  val snapshotInfoLabel = "SnapshotInfo"
  val snapshotInfo_KeyValue_Relation = "HAS_KEY_VALUE"

  implicit class SnapshotInfoImpl(snapshotInfo: SnapshotInfo) extends Neo4jRep[SnapshotInfo] {
    override def toNeo4jGraph(entity: SnapshotInfo): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(snapshotInfoLabel)(neo)
          node.setProperty("snapshotId", entity.snapshotId)
          node.setProperty("volumeId", entity.volumeId)
          node.setProperty("state", entity.state)
          node.setProperty("startTime", entity.startTime)
          node.setProperty("progress", entity.progress)
          node.setProperty("ownerId", entity.ownerId)
          node.setProperty("ownerAlias", entity.ownerAlias)
          node.setProperty("description", entity.description)
          node.setProperty("volumeSize", entity.volumeSize)
          entity.tags.foreach {
            tag =>
              val tagNode = tag.toNeo4jGraph(tag)
              repository.createRelation(snapshotInfo_KeyValue_Relation, node, tagNode)
          }
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SnapshotInfo] = {
      SnapshotInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[SnapshotInfo] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        if (repository.hasLabel(node, snapshotInfoLabel)) {
          val keyValueInfo = node.getRelationships.foldLeft(List.empty[AnyRef]) {
            (list, relationship) =>
              list.::(KeyValueInfo.fromNeo4jGraph(relationship.getEndNode.getId))
          }
          Some(SnapshotInfo(
            Some(nodeId),
            repository.getProperty[String](node, "snapshotId").get,
            repository.getProperty[String](node, "volumeId").get,
            repository.getProperty[String](node, "state").get,
            repository.getProperty[String](node, "startTime").get,
            repository.getProperty[String](node, "progress").get,
            repository.getProperty[String](node, "ownerId").get,
            repository.getProperty[String](node, "ownerAlias").get,
            repository.getProperty[String](node, "description").get,
            repository.getProperty[Int](node, "volumeSize").get,
            keyValueInfo.asInstanceOf[List[KeyValueInfo]]))
        } else {
          None
        }
    }
  }
}
