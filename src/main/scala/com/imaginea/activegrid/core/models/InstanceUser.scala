/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */

case class InstanceUser(override val id: Option[Long], userName: String, publicKeys: List[String]) extends BaseEntity

object InstanceUser {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[InstanceUser] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "userName", "publicKeys")
        val userName = map("userName").toString
        val publicKeys = map("publicKeys").asInstanceOf[Array[String]].toList

        Some(InstanceUser(Some(nodeId), userName, publicKeys))
      case None =>
        logger.warn(s"could not find node for InstanceUser with nodeId $nodeId")
        None
    }
  }

  implicit class InstanceUserImpl(instanceUser: InstanceUser) extends Neo4jRep[InstanceUser] {

    override def toNeo4jGraph(entity: InstanceUser): Node = {
      val label = "InstanceUser"
      val mapPrimitives = Map("userName" -> entity.userName, "publicKeys" -> entity.publicKeys.toArray)
      val node = Neo4jRepository.saveEntity[InstanceUser](label, entity.id, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[InstanceUser] = {
      InstanceUser.fromNeo4jGraph(id)
    }
  }

}
