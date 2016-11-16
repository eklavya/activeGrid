package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.{Node, NotFoundException, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 14/10/16.
  */
case class APMServerDetails(override val id: Option[Long],
                            name: String,
                            serverUrl: String,
                            monitoredSite: Option[Site1],
                            provider: APMProvider,
                            headers: Option[Map[String, String]]) extends BaseEntity

object APMServerDetails {
  val neo4JRepository = Neo4jRepository
  val logger = LoggerFactory.getLogger(getClass)
  val apmServerDetailsLabel = "APMServerDetails"
  val headersLabel = "Headers"
  val apmServer_site_relation = "HAS"
  val apmServer_header_relation = "HAS_HEADERS"

  implicit class APMServerDetailImpl(aPMServerDetails: APMServerDetails) extends Neo4jRep[APMServerDetails] {

    override def toNeo4jGraph(entity: APMServerDetails): Node = {
      logger.debug(s"Executing $getClass ::toNeo4jGraph ")
      neo4JRepository.withTx {
        neo =>
          val node = neo4JRepository.createNode(apmServerDetailsLabel)(neo)
          if (!aPMServerDetails.name.isEmpty) node.setProperty("name", aPMServerDetails.name)
          node.setProperty("serverUrl", aPMServerDetails.serverUrl)
          node.setProperty("provider", aPMServerDetails.provider.toString)
          aPMServerDetails.headers match {
            case Some(headers) =>
              val headersNode = neo4JRepository.createNode(headersLabel)(neo)
              headers.foreach { case (key, value) => headersNode.setProperty(key, value) }
              createRelationShip(node, headersNode, apmServer_header_relation)
            case None => None
          }
          aPMServerDetails.monitoredSite match {
            case Some(site) =>
              val siteNode = site.toNeo4jGraph(site)
              createRelationShip(node, siteNode, apmServer_site_relation)
            case None => None
          }
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[APMServerDetails] = {
      logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
      APMServerDetails.fromNeo4jGraph(nodeId)
    }

    def createRelationShip(parentNode: Node, childNode: Node, relationship: String): Unit = {
      logger.debug(s"Executing $getClass :: createRelationShip")
      neo4JRepository.withTx {
        neo =>
          parentNode.createRelationshipTo(childNode, new RelationshipType {
            override def name(): String = relationship
          })
      }
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[APMServerDetails] = {
    logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
    neo4JRepository.withTx {
      neo =>
        try {
          val node: Node = neo4JRepository.getNodeById(nodeId)(neo)
          if (neo4JRepository.hasLabel(node, apmServerDetailsLabel)) {

            val siteAndHeaders = node.getRelationships.foldLeft((Site1.apply(1), Map.empty[String, String])) {
              (result, relationsip) =>
                val childNode = relationsip.getEndNode
                relationsip.getType.name match {
                  case `apmServer_site_relation` => val site = Site1.fromNeo4jGraph(childNode.getId)
                    if (site.nonEmpty) (site.get, result._2) else result
                  case `apmServer_header_relation` => (result._1, childNode.getAllProperties.foldLeft(Map[String, String]())((map, property) =>
                    map + ((property._1, property._2.asInstanceOf[String]))))
                  case _ => result


                }
            }
            Some(new APMServerDetails(
              Some(node.getId),
              neo4JRepository.getProperty[String](node, "name").get,
              neo4JRepository.getProperty[String](node, "serverUrl").get,
              Option(siteAndHeaders._1.asInstanceOf[Site1]),
              APMProvider.toProvider(neo4JRepository.getProperty[String](node, "provider").get),
              Option(siteAndHeaders._2)
            ))
          } else {
            logger.warn(s"Node is not found with ID:$nodeId and Label : $apmServerDetailsLabel")
            None
          }
        } catch {
          case nfe: NotFoundException =>
            logger.warn(nfe.getMessage, nfe)
            None
        }
    }
  }

  def getAllEntities: List[Node] = {
    logger.debug(s"Executing $getClass ::getAllEntities ")
    neo4JRepository.withTx {
      neo =>
        val nodes = neo4JRepository.getAllNodesWithLabel(apmServerDetailsLabel)(neo)
        nodes.toList
    }
  }
}
