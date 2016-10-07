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

      val label: String = "Software"

      val mapPrimitives : Map[String, Any] = Map( "version"     -> entity.version,
        "name"        -> entity.name,
        "provider"    -> entity.provider,
        "downloadURL" -> entity.downloadURL,
        "port"        -> entity.port,
        "processNames"-> entity.processNames.toArray,
        "discoverApplications" -> entity.discoverApplications)

      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[TestImplicit](label, mapPrimitives)

      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Software] = {

      val listOfKeys: List[String] = List("version","name","provider","downloadURL","port","processNames","discoverApplications")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val version: String = propertyValues.get("version").get.toString
      val name: String = propertyValues.get("name").get.toString
      val provider: String = propertyValues.get("provider").get.toString
      val downloadURL: String = propertyValues.get("downloadURL").get.toString
      val port: String = propertyValues.get("port").get.toString
      val processNames: List[String] = propertyValues.get("processNames").get.asInstanceOf[Array[String]].toList
      val discoverApplications: Boolean = propertyValues.get("discoverApplications").get.toString.toBoolean
      Some(Software(Some(nodeId),version, name, provider, downloadURL, port, processNames,discoverApplications))
    }

  }


}