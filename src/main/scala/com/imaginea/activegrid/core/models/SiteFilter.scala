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
  val siteFilterAndAccountRelation = "HAS_ACCOUNT_INFO"
  val siteFilterAndFiltersRelation = "HAS_FILTERS"
  val logger = LoggerFactory.getLogger(getClass)

  def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph")
    val maybeNode = Neo4jRepository.findNodeById(nodeId)
    maybeNode.flatMap {
      node =>
        if (Neo4jRepository.hasLabel(node, siteFilterLabel)) {
          val mayBeAccountInfo = Neo4jRepository.getChildNodeId(nodeId, siteFilterAndAccountRelation).flatMap(id => AccountInfo.fromNeo4jGraph(id))
          val accountInfo = mayBeAccountInfo match {
            case Some(account) => account
            case None =>
              logger.warn(s"Account info is not found with relation $siteFilterAndAccountRelation")
              throw new NotFoundException(s"Account info is not found with relation $siteFilterAndAccountRelation")
          }
          val filterNodeIds: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteFilterAndFiltersRelation)
          val filters: List[Filter] = filterNodeIds.flatMap { childId =>
            Filter.fromNeo4jGraph(childId)
          }
          Some(SiteFilter(Some(node.getId), accountInfo, filters))
        } else {
          None
        }
    }
  }

  implicit class SiteFilterImpl(siteFilter: SiteFilter) extends Neo4jRep[SiteFilter] {
    override def toNeo4jGraph(entity: SiteFilter): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph")

      val node = Neo4jRepository.saveEntity[SiteFilter](siteFilterLabel, entity.id, Map.empty[String, Any])
      val accountInfoNode = entity.accountInfo.toNeo4jGraph(entity.accountInfo)
      Neo4jRepository.createRelation(siteFilterAndAccountRelation, node, accountInfoNode)
      entity.filters.foreach {
        filter =>
          val filterNode = filter.toNeo4jGraph(filter)
          Neo4jRepository.createRelation(siteFilterAndFiltersRelation, node, filterNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = {
      SiteFilter.fromNeo4jGraph(nodeId)
    }
  }
}
