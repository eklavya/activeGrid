package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 2/1/17.
  */
case class AnsiblePlay(override val id: Option[Long],
                       name: Option[String],
                       description: Option[String],
                       language: Option[ScriptType],
                       version: Option[String],
                       module: Option[Module],
                       arguments: List[ScriptArgument],
                       dependencies: Option[PuppetScriptDependencies],
                       pattern: Option[String],
                       taskList: List[Task],
                       content: String,
                       path: Option[String],
                       group: Option[AnsibleGroup]) extends Script

object AnsiblePlay {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val labelName = "AnsiblePlay"
  val ansiblePlayAndTask = "HAS_Task"
  val ansiblePlayAndAnsibleGroup = "HAS_AnsibleGroup"
  val scriptDefAndModule = "HAS_Module"
  val scriptDefAndScriptArg = "HAS_ScriptArgument"
  val scriptDefAndPuppetDependency = "HAS_PuppetDependency"

  def apply(name: Option[String],taskList: List[Task], content: String, group: Option[AnsibleGroup]): AnsiblePlay =
    AnsiblePlay(None, name, None, None, None, None, List.empty[ScriptArgument], None, None, taskList,
      content, None, group)

  implicit class AnsiblePlayImpl(ansiblePlay: AnsiblePlay) extends Neo4jRep[AnsiblePlay] {
    override def toNeo4jGraph(entity: AnsiblePlay): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "language" -> entity.language.map(_.scriptType),
        "version" -> entity.version,
        "pattern" -> entity.pattern,
        "content" -> entity.content,
        "path" -> entity.path)
      val parentNode = Neo4jRepository.saveEntity[AnsiblePlay](labelName, entity.id, map)
      entity.module match {
        case Some(m) =>
          val moduleNode = m.toNeo4jGraph(m)
          Neo4jRepository.createRelation(scriptDefAndModule, parentNode, moduleNode)
        case None => logger.debug("entity AnsiblePlay has no module")
      }
      entity.arguments.foreach { argument =>
        val argumentNode = argument.toNeo4jGraph(argument)
        Neo4jRepository.createRelation(scriptDefAndScriptArg, parentNode, argumentNode)
      }
      entity.dependencies match {
        case Some(puppetDependencies) =>
          val puppetNode = puppetDependencies.toNeo4jGraph(puppetDependencies)
          Neo4jRepository.createRelation(scriptDefAndPuppetDependency, parentNode, puppetNode)
        case None => logger.debug("entity AnsiblePlay has no puppet script dependencies")
      }
      entity.taskList.foreach {
        task =>
          val childNode = task.toNeo4jGraph(task)
          Neo4jRepository.createRelation(ansiblePlayAndTask, parentNode, childNode)
      }
      entity.group match {
        case Some(ansibleGroup) =>
          val ansibleGroupNode = ansibleGroup.toNeo4jGraph(ansibleGroup)
          Neo4jRepository.createRelation(ansiblePlayAndAnsibleGroup, parentNode, ansibleGroupNode)
        case None => logger.debug("entity AnsiblePlay has no ansible group")
      }
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
      puppetNodeId <- Neo4jRepository.getChildNodeId(id, scriptDefAndPuppetDependency)
      ansibleGroupNodeId <- Neo4jRepository.getChildNodeId(id, ansiblePlayAndAnsibleGroup)
    } yield {
      val map = Neo4jRepository.getProperties(node, "name", "description", "language", "version", "pattern", "content", "path")
      val argumentNodeIds = Neo4jRepository.getChildNodeIds(id, scriptDefAndScriptArg)
      val arguments = argumentNodeIds.flatMap(nodeId => ScriptArgument.fromNeo4jGraph(nodeId))
      val childNodeIds = Neo4jRepository.getChildNodeIds(id, ansiblePlayAndTask)
      val taskList = childNodeIds.flatMap(childId => Task.fromNeo4jGraph(childId))
      val module = Module.fromNeo4jGraph(moduleNodeId)
      val puppetDependecies = PuppetScriptDependencies.fromNeo4jGraph(puppetNodeId)
      val group = AnsibleGroup.fromNeo4jGraph(ansibleGroupNodeId)
      AnsiblePlay(Some(id),
        map.get("name").asInstanceOf[Option[String]],
        map.get("description").asInstanceOf[Option[String]],
        map.get("language").asInstanceOf[Option[String]].map(ScriptType.toScriptType),
        map.get("version").asInstanceOf[Option[String]],
        module,
        arguments,
        puppetDependecies,
        map.get("pattern").asInstanceOf[Option[String]],
        taskList,
        map("content").asInstanceOf[String],
        map.get("path").asInstanceOf[Option[String]],
        group
      )
    }
  }
}