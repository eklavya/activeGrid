package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

import scala.collection.JavaConversions._

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
  val repository = Neo4jRepository
  val instanceBlockDeviceMappingInfoLabel = "InstanceBlockDeviceMappingInfo"
  val ibd_VolumeInfo_Relation = "HAS_VOLUME_INFO"

  implicit class InstanceBlockDeviceMappingInfoImpl(instanceBlockDeviceMappingInfo: InstanceBlockDeviceMappingInfo) extends Neo4jRep[InstanceBlockDeviceMappingInfo] {
    override def toNeo4jGraph(entity: InstanceBlockDeviceMappingInfo): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(instanceBlockDeviceMappingInfoLabel) (neo)
          node.setProperty("deviceName", entity.deviceName)
          node.setProperty("status", entity.status)
          node.setProperty("attachTime", entity.attachTime)
          node.setProperty("deleteOnTermination", entity.deleteOnTermination)
          node.setProperty("usage", entity.usage)
          val childNode = entity.volume.toNeo4jGraph(entity.volume)
          repository.createRelation(ibd_VolumeInfo_Relation, node, childNode)
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
      InstanceBlockDeviceMappingInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        if (repository.hasLabel(node, ibd_VolumeInfo_Relation)) {
          val volumeInfoObj = node.getRelationships.foldLeft(Option[AnyRef](AnyRef)) {
            (reference, relationship) =>
              val childNode = relationship.getEndNode
              VolumeInfo.fromNeo4jGraph(childNode.getId)
          }
          Some(InstanceBlockDeviceMappingInfo(
            Some(nodeId),
            repository.getProperty[String](node, "deviceName").get,
            volumeInfoObj.asInstanceOf[VolumeInfo],
            repository.getProperty[String](node, "status").get,
            repository.getProperty[String](node, "attachTime").get,
            repository.getProperty[Boolean](node, "deleteOnTermination").get,
            repository.getProperty[Int](node, "usage").get))
        } else {
          None
        }
    }
  }
}
