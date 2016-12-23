package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class Variable(override val id: Option[Long],
                    name: String,
                    value: String,
                    scope: VariableScope,
                    readOnly: Boolean,
                    hidden: Boolean) extends BaseEntity

object Variable {
  val labelName = "Variable"

  implicit class VariableImpl(variable: Variable) extends Neo4jRep[Variable] {

    override def toNeo4jGraph(entity: Variable): Node = {
      val map = Map("name" -> entity.name,
        "value" -> entity.value,
        "scope" -> entity.scope.variableScope,
        "readOnly" -> entity.readOnly,
        "hidden" -> entity.hidden)
      Neo4jRepository.saveEntity[Variable](labelName, entity.id, map)
    }

    override def fromNeo4jGraph(id: Long): Option[Variable] = {
      Variable.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Variable] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "value", "scope", "readOnly", "hidden")
      Variable(Some(id),
        map("name").asInstanceOf[String],
        map("value").asInstanceOf[String],
        VariableScope.toVariableScope(map("scope").asInstanceOf[String]),
        ActiveGridUtils.convertToBooleanValue(map("readOnly")),
        ActiveGridUtils.convertToBooleanValue(map("hidden"))
      )

    }
  }
}
