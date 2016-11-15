package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 25/10/16.
  */
case class SiteFilter(override val id: Option[Long],
                      accountInfo: AccountInfo,
                      filters: List[Filter]) extends BaseEntity

object SiteFilter {

  val siteFilterLabel = "SiteFilter"
  val siteFilter_AccountInfo_Rel = "HAS_ACCOUNT_INFO"
  val siteFilter_Filters_Rel = "HAS_FILTERS"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class SiteFilterImpl(siteFilter: SiteFilter) extends Neo4jRep[SiteFilter] {
    override def toNeo4jGraph(entity: SiteFilter): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph")
      val node = Neo4jRepository.saveEntity[SiteFilter](siteFilterLabel, entity.id, Map.empty[String, Any])
      val accountInfoNode = entity.accountInfo.toNeo4jGraph(entity.accountInfo)
      Neo4jRepository.createRelation(siteFilter_AccountInfo_Rel, node, accountInfoNode)
      entity.filters.foreach {
        filter =>
          val filterNode = filter.toNeo4jGraph(filter)
          Neo4jRepository.createRelation(siteFilter_Filters_Rel, node, filterNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = {
      SiteFilter.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph")
    try {
      val maybeNode = Neo4jRepository.findNodeById(nodeId)
      maybeNode match {
        case Some(node) =>
          if (Neo4jRepository.hasLabel(node, siteFilterLabel)) {

            val childNodeIds_accountInfos: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteFilter_AccountInfo_Rel)
            val accountInfos: List[AccountInfo] = childNodeIds_accountInfos.flatMap { childId =>
              AccountInfo.fromNeo4jGraph(childId)
            }

            val childNodeIds_filter: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteFilter_Filters_Rel)
            val filters: List[Filter] = childNodeIds_filter.flatMap { childId =>
              Filter.fromNeo4jGraph(childId)
            }
            Some(SiteFilter(Some(node.getId), accountInfos.head, filters))
          } else {
            None
          }
        case None => None
      }

    } catch {
      case nfe: NotFoundException =>
        logger.warn(nfe.getMessage, nfe)
        None
    }
  }
}
