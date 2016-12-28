package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class PuppetScriptDependencies(override val id: Option[Long],
                                    files: List[ScriptFile],
                                    templates: List[ScriptFile],
                                    facterFiles: List[ScriptFile]) extends BaseEntity

object PuppetScriptDependencies {
  val labelName = "PuppetScriptDependencies"
  val puppetAndFile = "HAS_File"
  val puppetAndTemplate = "HAS_Template"
  val puppetAndFacterFile = "HAS_FacterFile"

  implicit class PuppetScriptDependenciesImpl(puppetScriptDependencies: PuppetScriptDependencies) extends Neo4jRep[PuppetScriptDependencies] {
    override def toNeo4jGraph(entity: PuppetScriptDependencies): Node = {
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, Map.empty[String, Any])
      entity.files.foreach { file =>
        val childNode = file.toNeo4jGraph(file)
        Neo4jRepository.createRelation(puppetAndFile, parentNode, childNode)
      }
      entity.templates.foreach { template =>
        val childNode = template.toNeo4jGraph(template)
        Neo4jRepository.createRelation(puppetAndTemplate, parentNode, childNode)
      }
      entity.facterFiles.foreach { facterFile =>
        val childNode = facterFile.toNeo4jGraph(facterFile)
        Neo4jRepository.createRelation(puppetAndFacterFile, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[PuppetScriptDependencies] = {
      PuppetScriptDependencies.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[PuppetScriptDependencies] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val fileIds = Neo4jRepository.getChildNodeIds(id, puppetAndFile)
      val files = fileIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      val templateIds = Neo4jRepository.getChildNodeIds(id, puppetAndTemplate)
      val templates = templateIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      val facterIds = Neo4jRepository.getChildNodeIds(id, puppetAndFacterFile)
      val facterFiles = facterIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      PuppetScriptDependencies(Some(id), files, templates, facterFiles)
    }
  }
}