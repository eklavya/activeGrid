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
                            language: Option[ScriptType],
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

  implicit class ScriptDefinitionImpl(scriptDefinition: ScriptDefinition) extends Neo4jRep[ScriptDefinition] {
    override def toNeo4jGraph(entity: ScriptDefinition): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "language" -> entity.language.map(_.scriptType),
        "version" -> entity.version)
      val parentNode = Neo4jRepository.saveEntity[ScriptDefinition](labelName, entity.id, map)
      entity.module match {
        case Some(m) =>
          val moduleNode = m.toNeo4jGraph(m)
          Neo4jRepository.createRelation(scriptDefAndModule, parentNode, moduleNode)
        case None => logger.debug("entity ScriptDefinition has no module")
      }
      entity.arguments.foreach { argument =>
        val argumentNode = argument.toNeo4jGraph(argument)
        Neo4jRepository.createRelation(scriptDefAndScriptArg, parentNode, argumentNode)
      }
      entity.dependencies match {
        case Some(puppetDependencies) =>
          val puppetNode = puppetDependencies.toNeo4jGraph(puppetDependencies)
          Neo4jRepository.createRelation(scriptDefAndPuppetDependency, parentNode, puppetNode)
        case None => logger.debug("entity ScriptDefiniton has no puppet script dependencies")
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
      puppetNodeId <- Neo4jRepository.getChildNodeId(id, scriptDefAndPuppetDependency)
    } yield {
      val map = Neo4jRepository.getProperties(node, "name", "description", "language", "version")
      val argumentNodeIds = Neo4jRepository.getChildNodeIds(id, scriptDefAndScriptArg)
      val arguments = argumentNodeIds.flatMap(nodeId => ScriptArgument.fromNeo4jGraph(nodeId))
      val module = Module.fromNeo4jGraph(moduleNodeId)
      val puppetDependecies = PuppetScriptDependencies.fromNeo4jGraph(puppetNodeId)
      ScriptDefinition(Some(id),
        map.get("name").asInstanceOf[Option[String]],
        map.get("description").asInstanceOf[Option[String]],
        map.get("language").asInstanceOf[Option[String]].map(ScriptType.toScriptType),
        map.get("version").asInstanceOf[Option[String]],
        module,
        arguments,
        puppetDependecies)
    }
  }
}