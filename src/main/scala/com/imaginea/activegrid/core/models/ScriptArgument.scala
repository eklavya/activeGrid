package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptArgument(override val id: Option[Long],
                          propName: Option[String],
                          propValue: Option[String],
                          argOrder: Option[Int],
                          nestedArg: Option[ScriptArgument],
                          value: String) extends BaseEntity

object ScriptArgument {
  val labelName = "ScriptArgument"
  val nestedRelation = "HAS_ScriptArgument"

  def apply(value: String): ScriptArgument =
    ScriptArgument(None, None, None, None, None, value)

  implicit class ScriptArgumentImpl(scriptArgument: ScriptArgument) extends Neo4jRep[ScriptArgument] {
    override def toNeo4jGraph(entity: ScriptArgument): Node = {
      val map = Map("propName" -> entity.propName,
        "propValue" -> entity.propValue,
        "argOrder" -> entity.argOrder,
        "value" -> entity.value)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.nestedArg.foreach { scriptArg =>
        val childNode = scriptArg.toNeo4jGraph(scriptArg)
        Neo4jRepository.createRelation(nestedRelation, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[ScriptArgument] = {
      ScriptArgument.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[ScriptArgument] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "propName", "propValue", "argOrder", "value")
      val scriptArgId = Neo4jRepository.getChildNodeId(id, nestedRelation)
      val scriptArg = scriptArgId.flatMap(nodeId => ScriptArgument.fromNeo4jGraph(nodeId))
      ScriptArgument(Some(id),
        map.get("propName").asInstanceOf[Option[String]],
        map.get("propValue").asInstanceOf[Option[String]],
        map.get("argOrder").asInstanceOf[Option[Int]],
        scriptArg,
        map("value").asInstanceOf[String])
    }
  }
}
