package com.imaginea.activegrid.core.models

import java.io.File

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptFile(override val id: Option[Long],
                      name: String,
                      path: String,
                      file: Option[File]) extends BaseEntity

object ScriptFile {
  val labelName = "ScriptFile"

  implicit class ScriptFileImpl(scriptFile: ScriptFile) extends Neo4jRep[ScriptFile] {
    override def toNeo4jGraph(entity: ScriptFile): Node = {
      val map = Map("name" -> entity.name, "path" -> entity.path)
      Neo4jRepository.saveEntity(labelName, entity.id, map)
    }

    override def fromNeo4jGraph(id: Long): Option[ScriptFile] = {
      ScriptFile.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[ScriptFile] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map {
      node =>
        val map = Neo4jRepository.getProperties(node, "name", "path")
        ScriptFile(Some(id), map("name").asInstanceOf[String], map("path").asInstanceOf[String], None)
    }
  }
}
