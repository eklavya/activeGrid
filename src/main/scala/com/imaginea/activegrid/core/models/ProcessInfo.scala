package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class ProcessInfo(override val id: Option[Long],
                       pid: Option[Int],
                       parentPid: Option[Int],
                       name: Option[String],
                       command: Option[String],
                       owner: Option[String],
                       residentBytes: Option[Long],
                       software: Option[Software],
                       softwareVersion: Option[String]) extends BaseEntity

object ProcessInfo {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def apply(pid: Int, parentPid: Int, name: String): ProcessInfo =
    ProcessInfo(None, Some(pid), Some(parentPid), Some(name), None, None, None, None, None)

  def fromNeo4jGraph(nodeId: Long): Option[ProcessInfo] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "pid", "parentPid", "name", "command", "owner", "residentBytes", "softwareVersion")
        val pid = ActiveGridUtils.getValueFromMapAs[Int](map, "pid")
        val parentPid = ActiveGridUtils.getValueFromMapAs[Int](map, "parentPid")
        val name = ActiveGridUtils.getValueFromMapAs[String](map, "name")
        val command = ActiveGridUtils.getValueFromMapAs[String](map, "command")
        val owner = ActiveGridUtils.getValueFromMapAs[String](map, "owner")
        val residentBytes = ActiveGridUtils.getValueFromMapAs[Long](map, "residentBytes")
        val softwareVersion = ActiveGridUtils.getValueFromMapAs[String](map, "softwareVersion")
        val relationship = "HAS_software"
        val software = Neo4jRepository.getChildNodeId(nodeId, relationship).flatMap(id => Software.fromNeo4jGraph(id))

        Some(ProcessInfo(Some(nodeId), pid, parentPid, name, command, owner, residentBytes, software, softwareVersion))
      case None =>
        logger.warn(s"could not find node for ProcessInfo with nodeId $nodeId")
        None
    }

  }

  implicit class ProcessInfoImpl(processInfo: ProcessInfo) extends Neo4jRep[ProcessInfo] {

    override def toNeo4jGraph(entity: ProcessInfo): Node = {
      val label = "ProcessInfo"
      val mapPrimitives = Map("pid" -> entity.pid,
        "parentPid" -> entity.parentPid,
        "name" -> entity.name,
        "command" -> entity.command,
        "owner" -> entity.owner,
        "residentBytes" -> entity.residentBytes,
        "softwareVersion" -> entity.softwareVersion)
      val node = Neo4jRepository.saveEntity[ProcessInfo](label, entity.id, mapPrimitives)

      entity.software match {
        case Some(soft) =>
          val node2 = soft.toNeo4jGraph(soft)
          val relationship = "HAS_software"
          Neo4jRepository.setGraphRelationship(node, node2, relationship)
          node

        case None => node
      }
    }

    override def fromNeo4jGraph(id: Long): Option[ProcessInfo] = {
      ProcessInfo.fromNeo4jGraph(id)
    }
  }

}