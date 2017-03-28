package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptDefinition(override val id: Option[Long],
                            override val name: Option[String],
                            override val description: Option[String],
                            override val language: ScriptType,
                            override val version: Option[String],
                            override val module: Option[Module],
                            override val arguments: List[ScriptArgument],
                            override val dependencies: Option[PuppetScriptDependencies]) extends Script

object ScriptDefinition {
  val labelName = "ScriptDefinition"
  val scriptDefAndModule = "HAS_Module"
  val scriptDefAndScriptArg = "HAS_ScriptArgument"
  val scriptDefAndPuppetDependency = "HAS_PuppetDependency"
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def apply(language: ScriptType): ScriptDefinition = {
    ScriptDefinition(None, None, None, language, None, None, List.empty[ScriptArgument], None)
  }

  implicit class ScriptDefinitionImpl(scriptDefinition: ScriptDefinition) extends Neo4jRep[ScriptDefinition] {
    override def toNeo4jGraph(entity: ScriptDefinition): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "language" -> entity.language.scriptType,
        "version" -> entity.version)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.module.map {
        module =>
          val childNode = module.toNeo4jGraph(module)
          Neo4jRepository.createRelation(scriptDefAndModule, parentNode, childNode)
      }

      entity.arguments.foreach { argument =>
        val argumentNode = argument.toNeo4jGraph(argument)
        Neo4jRepository.createRelation(scriptDefAndScriptArg, parentNode, argumentNode)
      }
      entity.dependencies.map {
        dependenoces =>
          val childNode = dependenoces.toNeo4jGraph(dependenoces)
          Neo4jRepository.createRelation(scriptDefAndPuppetDependency, parentNode, childNode)
      }

      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[ScriptDefinition] = {
      ScriptDefinition.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[ScriptDefinition] = {
    for {
      node <- Neo4jRepository.findNodeById(id)
      moduleNodeId <- Neo4jRepository.getChildNodeId(id, scriptDefAndModule)
      module <- Module.fromNeo4jGraph(moduleNodeId)
      puppetNodeId <- Neo4jRepository.getChildNodeId(id, scriptDefAndPuppetDependency)
      puppetDependecies <- PuppetScriptDependencies.fromNeo4jGraph(puppetNodeId)
    } yield {
      val map = Neo4jRepository.getProperties(node, "name", "description", "language", "version")
      val argumentNodeIds = Neo4jRepository.getChildNodeIds(id, scriptDefAndScriptArg)
      val arguments = argumentNodeIds.flatMap(nodeId => ScriptArgument.fromNeo4jGraph(nodeId))
      ScriptDefinition(Some(id),
        ActiveGridUtils.getValueFromMapAs(map, "name"),
        ActiveGridUtils.getValueFromMapAs(map, "description"),
        ScriptType.toScriptType(map("language").asInstanceOf[String]),
        ActiveGridUtils.getValueFromMapAs(map, "version"),
        Option(module),
        arguments,
        Option(puppetDependecies))
    }
  }
}