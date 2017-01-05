package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 2/1/17.
  */
case class AnsiblePlay(override val id: Option[Long],
                       name: String,
                       description: String,
                       language: ScriptType,
                       version: String,
                       module: Module,
                       arguments: List[ScriptArgument],
                       dependencies: PuppetScriptDependencies,
                       pattern: String,
                       taskList: List[Task],
                       content: String,
                       path: String,
                       group: AnsibleGroup) extends Script

object AnsiblePlay {
  val labelName = "AnsiblePlay"
  val ansiblePlayAndTask = "HAS_Task"
  val ansiblePlayAndAnsibleGroup = "HAS_AnsibleGroup"
  val scriptDefAndModule = "HAS_Module"
  val scriptDefAndScriptArg = "HAS_ScriptArgument"
  val scriptDefAndPuppetDependency = "HAS_PuppetDependency"

  implicit class AnsiblePlayImpl(ansiblePlay: AnsiblePlay) extends Neo4jRep[AnsiblePlay] {
    override def toNeo4jGraph(entity: AnsiblePlay): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "language" -> entity.language.scriptType,
        "version" -> entity.version,
        "pattern" -> entity.pattern,
        "content" -> entity.content,
        "path" -> entity.path)
      val parentNode = Neo4jRepository.saveEntity[AnsiblePlay](labelName, entity.id, map)
      val moduleNode = entity.module.toNeo4jGraph(entity.module)
      Neo4jRepository.createRelation(scriptDefAndModule, parentNode, moduleNode)
      entity.arguments.foreach { argument =>
        val argumentNode = argument.toNeo4jGraph(argument)
        Neo4jRepository.createRelation(scriptDefAndScriptArg, parentNode, argumentNode)
      }
      val puppetNode = entity.dependencies.toNeo4jGraph(entity.dependencies)
      Neo4jRepository.createRelation(scriptDefAndPuppetDependency, parentNode, puppetNode)
      entity.taskList.foreach {
        task =>
          val childNode = task.toNeo4jGraph(task)
          Neo4jRepository.createRelation(ansiblePlayAndTask, parentNode, childNode)
      }
      val ansibleGroupNode = entity.group.toNeo4jGraph(entity.group)
      Neo4jRepository.createRelation(ansiblePlayAndAnsibleGroup, parentNode, ansibleGroupNode)
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[AnsiblePlay] = {
      AnsiblePlay.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[AnsiblePlay] = {
    for {
      node <- Neo4jRepository.findNodeById(id)
      moduleNodeId <- Neo4jRepository.getChildNodeId(id, scriptDefAndModule)
      module <- Module.fromNeo4jGraph(moduleNodeId)
      puppetNodeId <- Neo4jRepository.getChildNodeId(id, scriptDefAndPuppetDependency)
      puppetDependecies <- PuppetScriptDependencies.fromNeo4jGraph(puppetNodeId)
      ansibleGroupNodeId <- Neo4jRepository.getChildNodeId(id, ansiblePlayAndAnsibleGroup)
      group <- AnsibleGroup.fromNeo4jGraph(ansibleGroupNodeId)
    } yield {
      val map = Neo4jRepository.getProperties(node, "name", "description", "language", "version", "pattern", "content", "path")
      val argumentNodeIds = Neo4jRepository.getChildNodeIds(id, scriptDefAndScriptArg)
      val arguments = argumentNodeIds.flatMap(nodeId => ScriptArgument.fromNeo4jGraph(nodeId))
      val childNodeIds = Neo4jRepository.getChildNodeIds(id, ansiblePlayAndTask)
      val taskList = childNodeIds.flatMap(childId => Task.fromNeo4jGraph(childId))
      AnsiblePlay(Some(id),
        map("name").asInstanceOf[String],
        map("description").asInstanceOf[String],
        ScriptType.toScriptType(map("language").asInstanceOf[String]),
        map("version").asInstanceOf[String],
        module,
        arguments,
        puppetDependecies,
        map("pattern").asInstanceOf[String],
        taskList,
        map("content").asInstanceOf[String],
        map("path").asInstanceOf[String],
        group
      )
    }
  }
}