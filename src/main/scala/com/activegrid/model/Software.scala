package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */

case class Software(override val id: Option[Long],version: String, name: String, provider: String, downloadURL: String, port: String, processNames: List[String],discoverApplications: Boolean)  extends BaseEntity

object Software{

  def fromNeo4jGraph(id: Option[Long]): Option[Software] = {

    id match {
      case Some(nodeId) =>
        val listOfKeys = List("version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")

        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val version = propertyValues("version").toString
        val name = propertyValues("name").toString
        val provider = propertyValues("provider").toString
        val downloadURL = propertyValues("downloadURL").toString
        val port = propertyValues("port").toString
        val processNames = propertyValues("processNames").asInstanceOf[Array[String]].toList
        val discoverApplications = propertyValues("discoverApplications").toString.toBoolean

        Some(Software(Some(nodeId), version, name, provider, downloadURL, port, processNames, discoverApplications))

      case None => None
    }
  }

  implicit class SoftwareImpl(software: Software) extends Neo4jRep[Software]{

    override def toNeo4jGraph(entity: Software): Node = {
      val label  = "Software"
      val mapPrimitives = Map( "version"     -> entity.version,
        "name"        -> entity.name,
        "provider"    -> entity.provider,
        "downloadURL" -> entity.downloadURL,
        "port"        -> entity.port,
        "processNames"-> entity.processNames.toArray,
        "discoverApplications" -> entity.discoverApplications)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[Software](label, mapPrimitives)

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[Software] = {

      Software.fromNeo4jGraph(id)
    }

  }
}