package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node


/**
  * Created by nagulmeeras on 18/10/16.
  */
case class PlugIn(override val id: Option[Long],
                  name: String,
                  active: Boolean) extends BaseEntity

object PlugIn {
  val labelName = "Plugin"

  implicit class PluginImpl(plugIn: PlugIn) extends Neo4jRep[PlugIn] {
    override def toNeo4jGraph(entity: PlugIn): Node = {
      val map = Map("name" -> entity.name,
        "active" -> entity.active)
      Neo4jRepository.saveEntity[PlugIn](labelName, entity.id, map)
    }

    override def fromNeo4jGraph(id: Long): Option[PlugIn] = {
      PlugIn.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[PlugIn] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "active")
      PlugIn(Some(id),
        map("name").asInstanceOf[String],
        map("active").asInstanceOf[Boolean]
      )
    }
  }
}

