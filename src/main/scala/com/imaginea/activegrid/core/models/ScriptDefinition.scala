package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptDefinition(override val id: Option[Long],
                            name: String,
                            description: String,
                            language: ScriptType,
                            version: String,
                            module: Module,
                            arguments: List[ScriptArgument],
                            dependencies: PuppetScriptDependencies) extends BaseEntity

object ScriptDefinition {
  val labelName = "ScriptDefinition"
  val scriptDefAndModule = "HAS_Module"
  val scriptDefAndScriptArg = "HAS_ScriptArgument"
  val scriptDefAndPuppetDependency = "HAS_PuppetDependency"

  implicit class ScriptDefinitionImpl(scriptDefinition: ScriptDefinition) extends Neo4jRep[ScriptDefinition] {
    override def toNeo4jGraph(entity: ScriptDefinition): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "language" -> entity.language.scriptType,
        "version" -> entity.version)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      val childNode = entity.module.toNeo4jGraph(entity.module)
      Neo4jRepository.createRelation(scriptDefAndModule, parentNode, childNode)
      entity.arguments.foreach { argument =>
        val argumentNode = argument.toNeo4jGraph(argument)
        Neo4jRepository.createRelation(scriptDefAndScriptArg, parentNode, argumentNode)
      }
      val puppetNode = entity.dependencies.toNeo4jGraph(entity.dependencies)
      Neo4jRepository.createRelation(scriptDefAndPuppetDependency, parentNode, puppetNode)
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[ScriptDefinition] = {

    }
  }

  def fromNeo4jGraph(id: Long): Option[ScriptDefinition] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "description", "language", "version")
      val moduleNodeId = Neo4jRepository.getChildNodeId(id, scriptDefAndModule)
      val mayBeModule = moduleNodeId.flatMap(nodeId => Module.fromNeo4jGraph(nodeId))
      val module = mayBeModule match {
        case Some(moduleEntity) => moduleEntity
        case None =>
          throw new Exception("Module entity is not found")
      }
      val argumentNodeIds = Neo4jRepository.getChildNodeIds(id, scriptDefAndScriptArg)
      val arguments = argumentNodeIds.flatMap(nodeId => ScriptArgument.fromNeo4jGraph(nodeId))
      val puppetNodeId = Neo4jRepository.getChildNodeId(id, scriptDefAndPuppetDependency)
      val mayBepuppetEntity = puppetNodeId.flatMap(nodeId => PuppetScriptDependencies.fromNeo4jGraph(nodeId))
      val puppetDependecies = mayBepuppetEntity match {
        case Some(puppetEntity) => puppetEntity
        case None =>
          throw new Exception("Puppet dependecy is not found")
      }
      ScriptDefinition(Some(id),
        map("name").asInstanceOf[String],
        map("description").asInstanceOf[String],
        ScriptType.toScriptType(map("language").asInstanceOf[String]),
        map("version").asInstanceOf[String],
        module,
        arguments,
        puppetDependecies)
    }
  }
}