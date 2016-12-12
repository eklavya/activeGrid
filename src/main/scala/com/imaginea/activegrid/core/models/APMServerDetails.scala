package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.{Node, NotFoundException, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

//scalastyle:ignore underscore.import

/**
  * Created by nagulmeeras on 14/10/16.
  */
case class APMServerDetails(override val id: Option[Long],
                            name: String,
                            serverUrl: String,
                            monitoredSite: Option[Site1],
                            provider: APMProvider,
                            headers: Map[String, String]) extends BaseEntity

object APMServerDetails {
  val neo4JRepository = Neo4jRepository
  val logger = LoggerFactory.getLogger(getClass)
  val apmServerDetailsLabel = "APMServerDetails"
  val headersLabel = "Headers"
  val apmServerAndSite = "HAS_Site"
  val apmServerAndHeaders = "HAS_HEADERS"

  val label = APMServerDetails.getClass.getName
  val relationLabel = ActiveGridUtils.relationLbl(label)

  def fromNeo4jGraph(nodeId: Long): Option[APMServerDetails] = {
    logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
    neo4JRepository.withTx {
      neo =>
        try {
          val node: Node = neo4JRepository.getNodeById(nodeId)(neo)
          if (neo4JRepository.hasLabel(node, apmServerDetailsLabel)) {

            val (site, headers) = node.getRelationships.foldLeft((Site1.apply(1), Map.empty[String, String])) {
              (result, relationship) =>
                val childNode = relationship.getEndNode
                val (site, headers) = result
                relationship.getType.name match {
                  case `apmServerAndSite` => val mayBeSite = Site1.fromNeo4jGraph(childNode.getId)
                    mayBeSite match {
                      case Some(siteObj) => (siteObj, headers)
                      case None => result
                    }
                  case `apmServerAndHeaders` =>
                    val propertyMap = childNode.getAllProperties.foldLeft(Map[String, String]()) { (map, property) =>
                      val (key, value) = property
                      map + ((key, value.asInstanceOf[String]))
                    }
                    (site, propertyMap)
                  case _ => result
                }
            }
            Some(new APMServerDetails(
              Some(node.getId),
              neo4JRepository.getProperty[String](node, "name").get,
              neo4JRepository.getProperty[String](node, "serverUrl").get,
              Option(site.asInstanceOf[Site1]),
              APMProvider.toProvider(neo4JRepository.getProperty[String](node, "provider").get),
              headers
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

  implicit class APMServerDetailImpl(aPMServerDetails: APMServerDetails) extends Neo4jRep[APMServerDetails] {

    override def toNeo4jGraph(entity: APMServerDetails): Node = {
      logger.debug(s"Executing $getClass ::toNeo4jGraph ")
      neo4JRepository.withTx {
        neo =>
          val node = neo4JRepository.createNode(apmServerDetailsLabel)(neo)
          if (!aPMServerDetails.name.isEmpty) node.setProperty("name", aPMServerDetails.name)
          node.setProperty("serverUrl", aPMServerDetails.serverUrl)
          node.setProperty("provider", aPMServerDetails.provider.toString)
          val headersNode = neo4JRepository.createNode(headersLabel)(neo)
          aPMServerDetails.headers.foreach { case (key, value) => headersNode.setProperty(key, value) }
          createRelationShip(node, headersNode, apmServerAndHeaders)

          aPMServerDetails.monitoredSite.foreach {
            site =>
              val siteNode = site.toNeo4jGraph(site)
              createRelationShip(node, siteNode, apmServerAndSite)
          }
          node
      }
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

    override def fromNeo4jGraph(nodeId: Long): Option[APMServerDetails] = {
      logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
      APMServerDetails.fromNeo4jGraph(nodeId)
    }
  }

}
