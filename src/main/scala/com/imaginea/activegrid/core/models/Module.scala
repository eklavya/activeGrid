package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 20/12/16.
  */
case class Module(override val id: Option[Long],
                  name: String,
                  path: String,
                  version: String,
                  definition: ModuleDefinition) extends BaseEntity

object Module {
  val labelName = "Module"
  val moduleAndPuppet = "HAS_PuppetModule"
  val moduleAndAnsible = "HAS_AnsibleModule"
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class ModuleImpl(module: Module) extends Neo4jRep[Module] {
    override def toNeo4jGraph(entity: Module): Node = {
      val map = Map("name" -> entity.name,
        "path" -> entity.path,
        "version" -> entity.version)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.definition match {
        case _: AnsibleModuleDefinition =>
          val ansibleInstance = entity.definition.asInstanceOf[AnsibleModuleDefinition]
          val ansibleNode = ansibleInstance.toNeo4jGraph(ansibleInstance)
          Neo4jRepository.createRelation(moduleAndAnsible, parentNode, ansibleNode)
        case _: PuppetModuleDefinition =>
          val puppetModuleDefinition = entity.definition.asInstanceOf[PuppetModuleDefinition]
          val puppetNode = puppetModuleDefinition.toNeo4jGraph(puppetModuleDefinition)
          Neo4jRepository.createRelation(moduleAndPuppet, parentNode, puppetNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[Module] = {
      Module.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Module] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "path", "varsion")
      val puppetModuleId = Neo4jRepository.getChildNodeId(id, moduleAndPuppet)
      val mayBeEntity = puppetModuleId match {
        case Some(puppetNodeId) => PuppetModuleDefinition.fromNeo4jGraph(puppetNodeId)
        case None => val ansibleModuleId = Neo4jRepository.getChildNodeId(id, moduleAndAnsible)
          ansibleModuleId.flatMap(moduleId => AnsibleModuleDefinition.fromNeo4jGraph(moduleId))
      }
      val moduleDefintionEntity = mayBeEntity match {
        case Some(entity) => entity
        case None =>
          logger.warn("No entity of module definition if found as it is mandatory")
          throw new Exception("Module definition entity is not found!")
      }
      Module(Some(id),
        map("name").asInstanceOf[String],
        map("path").asInstanceOf[String],
        map("version").asInstanceOf[String],
        moduleDefintionEntity)
    }
  }
}