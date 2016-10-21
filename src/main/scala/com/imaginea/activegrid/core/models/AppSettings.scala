package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

case class AppSettings(override val id:Option[Long],settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity

object AppSettings {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class RichAppSettings(appSettings: AppSettings) extends Neo4JRep[AppSettings] {
    override def toGraph(entity: AppSettings): Option[Node] = {
      AppSettingsNeo4jWrapper.toNeo4jGraph(entity)

    }

    override def fromGraph(nodeId: Long): AppSettings = {
       AppSettingsNeo4jWrapper.fromNeo4jGraph(0L)

    }
  }

}



