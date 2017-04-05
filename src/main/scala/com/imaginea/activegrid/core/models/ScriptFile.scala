package com.imaginea.activegrid.core.models

import java.io.File

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptFile(override val id: Option[Long],
                      name: Option[String],
                      path: Option[String],
                      file: Option[File]) extends BaseEntity

object ScriptFile {
  val labelName = "ScriptFile"

  def apply(file: File): ScriptFile =
    ScriptFile(None, Some(file.getName), Some(file.getPath), Some(file))

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
        ScriptFile(Some(id), ActiveGridUtils.getValueFromMapAs[String](map, "name"), ActiveGridUtils.getValueFromMapAs[String](map, "path"), None)
    }
  }
}
