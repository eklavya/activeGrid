package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class ProcessInfo(override val id: Option[Long],
                       pid: Int,
                       parentPid: Int,
                       name: String,
                       command: Option[String],
                       owner: Option[String],
                       residentBytes: Option[Long],
                       software: Option[Software],
                       softwareVersion: Option[String]) extends BaseEntity

object ProcessInfo {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def apply(pid: Int, parentPid: Int, name: String): ProcessInfo =
    ProcessInfo(None, 1, 1, "init", None, None, None, None, None)

  def fromNeo4jGraph(nodeId: Long): Option[ProcessInfo] = {
    val listOfKeys = List("pid", "parentPid", "name", "command", "owner", "residentBytes", "softwareVersion")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val pid = propertyValues("pid").toString.toInt
      val parentPid = propertyValues("parentPid").toString.toInt
      val name = propertyValues("name").toString
      val command = propertyValues.get("command").asInstanceOf[Option[String]]
      val owner = propertyValues.get("owner").asInstanceOf[Option[String]]
      val residentBytes = propertyValues.get("residentBytes").asInstanceOf[Option[Long]]
      val softwareVersion = propertyValues.get("softwareVersion").asInstanceOf[Option[String]]
      val relationship = "HAS_software"
      val software = GraphDBExecutor.getChildNodeId(nodeId, relationship).flatMap(id => Software.fromNeo4jGraph(id))

      Some(ProcessInfo(Some(nodeId), pid, parentPid, name, command, owner, residentBytes, software, softwareVersion))
    }
    else {
      logger.warn(s"could not get graph properties for ProcessInfo node with $nodeId")
      None
    }

  }

  implicit class ProcessInfoImpl(processInfo: ProcessInfo) extends Neo4jRep[ProcessInfo] {

    override def toNeo4jGraph(entity: ProcessInfo): Node = {
      val label = "ProcessInfo"
      val mapPrimitives = Map("pid" -> entity.pid,
        "parentPid" -> entity.parentPid,
        "name" -> entity.name,
        "command" -> entity.command.getOrElse(GraphDBExecutor.NO_VAL),
        "owner" -> entity.owner.getOrElse(GraphDBExecutor.NO_VAL),
        "residentBytes" -> entity.residentBytes.getOrElse(GraphDBExecutor.NO_VAL),
        "softwareVersion" -> entity.softwareVersion.getOrElse(GraphDBExecutor.NO_VAL))
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[ProcessInfo](label, mapPrimitives)

      entity.software match {
        case Some(soft) =>
          val node2 = soft.toNeo4jGraph(soft)
          val relationship = "HAS_software"
          GraphDBExecutor.setGraphRelationship(node, node2, relationship)
          node

        case None => node
      }
    }

    override def fromNeo4jGraph(id: Long): Option[ProcessInfo] = {
      ProcessInfo.fromNeo4jGraph(id)
    }
  }
}
