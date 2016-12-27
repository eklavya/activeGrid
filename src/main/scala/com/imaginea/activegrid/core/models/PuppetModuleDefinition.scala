package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 21/12/16.
  */
case class PuppetModuleDefinition(override val id: Option[Long],
                                  files: List[ScriptFile],
                                  templates: List[ScriptFile],
                                  facterFiles: List[ScriptFile],
                                  manifests: List[ScriptFile]) extends ModuleDefinition

object PuppetModuleDefinition {
  val labelName = "PuppetModuleDefinition"
  val puppetAndFile = "HAS_File"
  val puppetAndTemplate = "HAS_Template"
  val puppetAndFacterFile = "HAS_Factory"
  val puppetAndManifest = "HAS_Manifest"

  implicit class PuppetModuleDefinitionImpl(puppetModuleDefinition: PuppetModuleDefinition)
    extends Neo4jRep[PuppetModuleDefinition] {
    override def toNeo4jGraph(entity: PuppetModuleDefinition): Node = {
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
      entity.manifests.foreach { manifest =>
        val childNode = manifest.toNeo4jGraph(manifest)
        Neo4jRepository.createRelation(puppetAndManifest, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[PuppetModuleDefinition] = {
      PuppetModuleDefinition.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[PuppetModuleDefinition] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val fileIds = Neo4jRepository.getChildNodeIds(id, puppetAndFile)
      val files = fileIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      val templateIds = Neo4jRepository.getChildNodeIds(id, puppetAndTemplate)
      val templates = templateIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      val facterIds = Neo4jRepository.getChildNodeIds(id, puppetAndFacterFile)
      val facterFiles = facterIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      val manifestIds = Neo4jRepository.getChildNodeIds(id, puppetAndManifest)
      val manifests = manifestIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))

      PuppetModuleDefinition(Some(id), files, templates, facterFiles, manifests)
    }
  }
}
