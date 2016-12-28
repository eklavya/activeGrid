package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

import scala.collection.JavaConversions._

/**
  * Created by shareefn on 21/12/16.
  */
case class StepInput(override val id: Option[Long], values: Map[String, String]) extends BaseEntity

object StepInput {
  val labelName = "StepInput"

  implicit class StepInputImpl(stepInput: StepInput) extends Neo4jRep[StepInput] {
    override def toNeo4jGraph(entity: StepInput): Node = {
      Neo4jRepository.saveEntity(labelName, entity.id, entity.values)
    }

    override def fromNeo4jGraph(id: Long): Option[StepInput] = {
      StepInput.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[StepInput] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)

    mayBeNode.map { node =>
      Neo4jRepository.withTx { neo =>
        val map = node.getAllProperties
        StepInput(Some(id), map.map { case (key, value) => (key, value.asInstanceOf[String]) }.toMap)
      }
    }
  }
}