package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptDefinition(override val id: Option[Long],
                            name: Option[String],
                            description: Option[String],
                            language: ScriptType,
                            version: Option[String],
                            module: Option[Module],
                            arguments: List[ScriptArgument],
                            dependencies: Option[PuppetScriptDependencies]) extends Script

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
      val puppetNode = entity.dependencies.map {
        dependenoces =>
          val childNode = dependenoces.toNeo4jGraph(dependenoces)
          Neo4jRepository.createRelation(scriptDefAndPuppetDependency, parentNode, puppetNode)
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
        map.get("name").asInstanceOf[Option[String]],
        map.get("description").asInstanceOf[Option[String]],
        ScriptType.toScriptType(map("language").asInstanceOf[String]),
        map.get("version").asInstanceOf[Option[String]],
        Option(module),
        arguments,
        Option(puppetDependecies))
    }
  }
}