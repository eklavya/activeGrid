/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

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
  val instanceBlockDeviceMappingInfoLabel = "InstanceBlockDeviceMappingInfo"
  val ibd_VolumeInfo_Relation = "HAS_VOLUME_INFO"
  val logger = LoggerFactory.getLogger(getClass)

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
      Neo4jRepository.createRelation(ibd_VolumeInfo_Relation, node, childNode)
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
      InstanceBlockDeviceMappingInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[InstanceBlockDeviceMappingInfo] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, ibd_VolumeInfo_Relation)) {
          val volumeInfoObj = node.getRelationships.foldLeft(Option(VolumeInfo.apply(1))) {
            (reference, relationship) =>
              val childNode = relationship.getEndNode
              VolumeInfo.fromNeo4jGraph(childNode.getId)
          }
          val map = Neo4jRepository.getProperties(node, "deviceName", "status",
            "attachTime", "deleteOnTermination", "usage")

          Some(InstanceBlockDeviceMappingInfo(
            Some(nodeId),
            map("deviceName").asInstanceOf[String],
            volumeInfoObj.asInstanceOf[VolumeInfo],
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
