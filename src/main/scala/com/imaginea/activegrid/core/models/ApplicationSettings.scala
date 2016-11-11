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

case class ApplicationSettings(override val id: Option[Long], settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity

object ApplicationSettings {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class RichApplicationSettings(appSettings: ApplicationSettings) extends Neo4jRep[ApplicationSettings] {
    override def toNeo4jGraph(entity: ApplicationSettings): Node = {
      AppSettingsNeo4jWrapper.toNeo4jGraph(entity)
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ApplicationSettings] = {
      AppSettingsNeo4jWrapper.fromNeo4jGraph(0L)

    }
  }

}



