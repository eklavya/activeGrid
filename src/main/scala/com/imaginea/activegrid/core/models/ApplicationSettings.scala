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



