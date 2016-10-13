package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class ProcessInfo(override val id: Option[Long],pid: Int, parentPid: Int, name: String, command: String, owner: String, residentBytes: Long, software: Software, softwareVersion: String)  extends BaseEntity

object ProcessInfo{

  implicit class ProcessInfoImpl(processInfo: ProcessInfo) extends Neo4jRep[ProcessInfo] {

    override def toNeo4jGraph(entity: ProcessInfo): Option[Node] = {

      val label = "ProcessInfo"

      val mapPrimitives = Map("pid" -> entity.pid,
        "parentPid"-> entity.parentPid,
        "name" -> entity.name,
        "command" -> entity.command,
        "owner" -> entity.owner,
        "residentBytes" -> entity.residentBytes,
        "softwareVersion" -> entity.softwareVersion)

      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[ProcessInfo](label, mapPrimitives)

      val node2: Option[Node] = entity.software.toNeo4jGraph(entity.software)

      val relationship = "HAS_software"
      GraphDBExecutor.setGraphRelationship(node,node2,relationship)

      node

    }

    override def fromNeo4jGraph(nodeId: Long): ProcessInfo = {

      val listOfKeys = List("pid", "parentPid", "name", "command", "owner", "residentBytes", "softwareVersion")

      val propertyValues = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val pid = propertyValues.get("pid").get.toString.toInt
      val parentPid = propertyValues.get("parentPid").get.toString.toInt
      val name = propertyValues.get("name").get.toString
      val command = propertyValues.get("command").get.toString
      val owner = propertyValues.get("owner").get.toString
      val residentBytes = propertyValues.get("residentBytes").get.toString.toLong
      val softwareVersion = propertyValues.get("softwareVersion").get.toString

      val relationship = "HAS_software"
      val childNodeId = GraphDBExecutor.getChildNodeId(nodeId,relationship)

      val soft:Software = null
      val software: Software = soft.fromNeo4jGraph(childNodeId)

      ProcessInfo( Some(nodeId),pid, parentPid, name, command, owner, residentBytes, software, softwareVersion)

    }

  }

}
