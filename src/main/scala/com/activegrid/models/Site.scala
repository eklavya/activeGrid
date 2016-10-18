package com.activegrid.models

import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 14/10/16.
  */
case class Site(override val id: Option[Long],
                siteName: Option[String],
                groupBy: Option[String]) extends BaseEntity

object Site {
  val neo4JRepository = Neo4JRepository
  val logger = LoggerFactory.getLogger(getClass)

  implicit class SiteImpl(site: Site) extends Neo4jRep[Site] {
    val siteLabel = "Site"

    override def toNeo4jGraph(): Node = {
      logger.debug(s"Executing $getClass ::toNeo4jGraph ")
      try {
        neo4JRepository.withTx {
          neo =>
            val node = neo4JRepository.createNode(siteLabel)(neo)
            if (!site.siteName.isEmpty) node.setProperty("siteName", site.siteName.get)
            if (!site.groupBy.isEmpty) node.setProperty("groupBy", site.groupBy.get)
            node
        }
      } catch {
        case exception: Exception => throw new Exception(s"Unable to persist entity $site")
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Site = {
      logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
      Site.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Site = {
    logger.debug(s"Executing $getClass ::fromNeo4jGraph ")
    neo4JRepository.withTx {
      neo =>
        try {
          val node = neo4JRepository.getNodeById(nodeId)(neo)
          val site = new Site(Some(nodeId),
            if (node.hasProperty("siteName")) Some(node.getProperty("siteName").asInstanceOf[String]) else None,
            if (node.hasProperty("groupBy")) Some(node.getProperty("groupBy").asInstanceOf[String]) else None)
          site
        } catch {
          case nfe: NotFoundException => throw new Exception(s"Site entity with ID:$nodeId is Not Found")
        }
    }
  }
}

