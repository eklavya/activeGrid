/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 31/10/16.
  */
case class LoadBalancer(override val id: Option[Long],
                        name: String,
                        vpcId: String,
                        region: Option[String],
                        instanceIds: List[String],
                        availabilityZones: List[String]) extends BaseEntity

object LoadBalancer {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "LoadBalancer"

  def fromNeo4jGraph(id: Long): Option[LoadBalancer] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "name", "vpcId", "region", "instanceIds", "availabilityZones")

        val loadBalancer = LoadBalancer(
          Some(node.getId),
          map("name").toString,
          map("vpcId").toString,
          ActiveGridUtils.getValueFromMapAs[String](map, "region"),
          map("instanceIds").asInstanceOf[Array[String]].toList,
          map("availabilityZones").asInstanceOf[Array[String]].toList
        )
        Some(loadBalancer)
      case None => None
    }
  }

  implicit class RichLoadBalancer(loadBalancer: LoadBalancer) extends Neo4jRep[LoadBalancer] {

    override def toNeo4jGraph(entity: LoadBalancer): Node = {
      logger.debug(s"toGraph for LoadBalancer $entity")
      val map = Map(
        "name" -> entity.name,
        "vpcId" -> entity.vpcId,
        "region" -> entity.region,
        "instanceIds" -> entity.instanceIds.toArray,
        "availabilityZones" -> entity.availabilityZones.toArray
      )
      val node = Neo4jRepository.saveEntity[LoadBalancer](label, entity.id, map)
      logger.debug(s"node - $node")
      node
    }

    override def fromNeo4jGraph(id: Long): Option[LoadBalancer] = {
      LoadBalancer.fromNeo4jGraph(id)
    }
  }

}
