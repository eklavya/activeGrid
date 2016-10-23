package com.activegrid.models

import org.neo4j.graphdb.{Node, NotFoundException, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable


/**
  * Created by nagulmeeras on 14/10/16.
  */
case class APMServerDetails(override val id: Option[Long],
                            name: String,
                            serverUrl: String,
                            monitoredSite: Site,
                            provider: APMProvider,
                            headers: Option[Map[String, String]]) extends BaseEntity

object APMServerDetails {
  val neo4JRepository = Neo4JRepository
  val logger = LoggerFactory.getLogger(getClass)
  val apmServerDetailsLabel = "APMServerDetails"
  val headersLabel = "Headers"
  val apmServer_site_relation = "HAS"
  val apmServer_header_relation = "HAS_HEADERS"

  implicit class APMServerDetailImpl(aPMServerDetails: APMServerDetails) extends Neo4jRep[APMServerDetails] {

    override def toNeo4jGraph(): Node = {
      logger.debug(s"Executing $getClass ::toNeo4jGraph ")
      neo4JRepository.withTx {
        neo =>
          val node = neo4JRepository.createNode(apmServerDetailsLabel)(neo)
          if (!aPMServerDetails.name.isEmpty) node.setProperty("name", aPMServerDetails.name)
          node.setProperty("serverUrl", aPMServerDetails.serverUrl)
          node.setProperty("provider", aPMServerDetails.provider.toString)
          if (!aPMServerDetails.headers.isEmpty) {
            val headersNode = neo4JRepository.createNode(headersLabel)(neo)
            aPMServerDetails.headers.get.foreach { case (key, value) => headersNode.setProperty(key, value) }
            createRelationShip(node, headersNode, apmServer_header_relation)
          }
          val siteNode = aPMServerDetails.monitoredSite.toNeo4jGraph()
          logger.info("Saved Site Details ")
          createRelationShip(node, siteNode, apmServer_site_relation)
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): APMServerDetails = {
      logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
      APMServerDetails.fromNeo4jGraph(nodeId)
    }

    def createRelationShip(parentNode: Node, childNode: Node, relationship: String): Unit = {

      neo4JRepository.withTx {
        neo =>
          parentNode.createRelationshipTo(childNode, new RelationshipType {
            override def name(): String = relationship
          })
      }
    }
  }

  def fromNeo4jGraph(nodeId: Long): APMServerDetails = {
    logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
    try {
      neo4JRepository.withTx {
        neo =>

          val node: Node = neo4JRepository.getNodeById(nodeId)(neo)
          val itr = node.getRelationships.iterator
          var siteEntity: Site = null
          val header: mutable.Map[String, String] = mutable.Map.empty[String, String]
          while (itr.hasNext) {
            val relationship = itr.next()
            relationship.getType.name match {
              case `apmServer_site_relation` =>
                siteEntity = Site.fromNeo4jGraph(relationship.getEndNode.getId)

              case `apmServer_header_relation` =>
                relationship.getEndNode.getAllProperties.map { case (key, value) => header.put(key.toString, value.toString) }
            }

          }

          val aPMServerDetails1 = new APMServerDetails(Some(node.getId), node.getProperty("name").asInstanceOf[String], node.getProperty("serverUrl").asInstanceOf[String]
            , siteEntity, APMProvider.toProvider(node.getProperty("provider").asInstanceOf[String]), if (header.nonEmpty) Some(header.toMap) else None)
          aPMServerDetails1
      }
    }
    catch {
      case nfe: NotFoundException => throw new Exception(s"APM Server Details entity with $nodeId is Not Found")
      case exception: Exception => throw new Exception("Neo4j Exception")
    }
  }

  def getAllEntities(): List[Node] = {
    logger.debug(s"Executing $getClass ::getAllEntities ")
    neo4JRepository.withTx {
      neo =>
        val nodes = neo4JRepository.getAllNodesWithLabel(apmServerDetailsLabel)(neo)
        nodes.toList
    }
  }
}
