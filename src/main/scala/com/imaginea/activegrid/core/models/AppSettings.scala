package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

case class AppSettings(settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity

object AppSettings {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class RichAppSettings(appSettings: AppSettings) extends Neo4JRepo[AppSettings] {
    override def toGraph(entity: AppSettings): Option[Node] = {
      val maybeNode = AppSettingsNeo4jWrapper.toNeo4jGraph(entity)
      maybeNode
    }

    override def fromGraph(nodeId: Long): AppSettings = {
      val maybeSettings = AppSettingsNeo4jWrapper.fromNeo4jGraph(0L)
      maybeSettings
    }
  }

}



