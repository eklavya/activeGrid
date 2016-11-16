package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._ // scalastyle:ignore underscore.import

/**
  * Created by nagulmeeras on 25/10/16.
  */
case class SiteFilter(override val id: Option[Long],
                      accountInfo: AccountInfo,
                      filters: List[Filter]) extends BaseEntity

object SiteFilter {
  val repository = Neo4jRepository
  val siteFilterLabel = "SiteFilter"
  val siteFilter_AccountInfo_Rel = "HAS_ACCOUNT_INFO"
  val siteFilter_Filters_Rel = "HAS_FILTERS"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class SiteFilterImpl(siteFilter: SiteFilter) extends Neo4jRep[SiteFilter] {
    override def toNeo4jGraph(entity: SiteFilter): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph")
      repository.withTx {
        neo =>
          val node = repository.createNode(siteFilterLabel)(neo)
          val accountInfoNode = entity.accountInfo.toNeo4jGraph(entity.accountInfo)
          repository.createRelation(siteFilter_AccountInfo_Rel, node, accountInfoNode)
          entity.filters.foreach {
            filter =>
              val filterNode = filter.toNeo4jGraph(filter)
              repository.createRelation(siteFilter_Filters_Rel, node, filterNode)
          }
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = {
      SiteFilter.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph")
    repository.withTx {
      neo =>
        try {
          val node = repository.getNodeById(nodeId)(neo)
          if (repository.hasLabel(node, siteFilterLabel)) {

            val accountAndFilter = node.getRelationships.foldLeft((AccountInfo.apply(1), List[Filter]())) {
              (result, relationship) =>
                val childNode = relationship.getEndNode
                logger.info(s"Relation Name : ${relationship.getType.name} , Node ID : $nodeId")
                relationship.getType.name match {
                  case `siteFilter_AccountInfo_Rel` => val accountInfo = AccountInfo.fromNeo4jGraph(childNode.getId)
                    if (accountInfo.nonEmpty) (accountInfo.get, result._2) else result
                  case `siteFilter_Filters_Rel` => val filter = Filter.fromNeo4jGraph(childNode.getId)
                    if (filter.nonEmpty) (result._1, filter.get :: result._2) else result
                  case _ => result
                }
            }
            Some(SiteFilter(Some(node.getId), accountAndFilter._1.asInstanceOf[AccountInfo], accountAndFilter._2))
          } else {
            None
          }
        } catch {
          case nfe: NotFoundException =>
            logger.warn(nfe.getMessage, nfe)
            None
        }
    }
  }
}
