package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */

case class Software(override val id: Option[Long],version: String, name: String, provider: String, downloadURL: String, port: String, processNames: List[String],discoverApplications: Boolean)  extends BaseEntity

object Software{

  implicit class SoftwareImpl(software: Software) extends Neo4jRep[Software]{

    override def toNeo4jGraph(entity: Software): Option[Node] = {
      val label  = "Software"
      val mapPrimitives = Map( "version"     -> entity.version,
        "name"        -> entity.name,
        "provider"    -> entity.provider,
        "downloadURL" -> entity.downloadURL,
        "port"        -> entity.port,
        "processNames"-> entity.processNames.toArray,
        "discoverApplications" -> entity.discoverApplications)
      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[Software](label, mapPrimitives)

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[Software] = {

      Software.fromNeo4jGraph(id)
    }

  }

  def fromNeo4jGraph(id: Option[Long]): Option[Software] = {

    id match {
      case Some(nodeId) => {
        val listOfKeys = List("version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")

        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val version = propertyValues.get("version").get.toString
        val name = propertyValues.get("name").get.toString
        val provider = propertyValues.get("provider").get.toString
        val downloadURL = propertyValues.get("downloadURL").get.toString
        val port = propertyValues.get("port").get.toString
        val processNames = propertyValues.get("processNames").get.asInstanceOf[Array[String]].toList
        val discoverApplications = propertyValues.get("discoverApplications").get.toString.toBoolean

        Some(Software(Some(nodeId), version, name, provider, downloadURL, port, processNames, discoverApplications))
      }
      case None => None
    }
  }
}