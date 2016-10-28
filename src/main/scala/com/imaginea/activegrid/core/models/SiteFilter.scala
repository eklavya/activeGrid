package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

import scala.collection.JavaConversions._

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

  implicit class SiteFilterImpl(siteFilter: SiteFilter) extends Neo4jRep[SiteFilter] {
    override def toNeo4jGraph(entity: SiteFilter): Node = {
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
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        if (repository.hasLabel(node, siteFilterLabel)) {

          val tupleObj = node.getRelationships.foldLeft(Tuple2[AnyRef, List[Filter]](AnyRef, List[Filter]())) {
            (tuple, relationship) =>
              val childNode = relationship.getEndNode
              relationship.getType.name match {
                case `siteFilter_AccountInfo_Rel` => Tuple2(AccountInfo.fromNeo4jGraph(childNode.getId).get, tuple._2)
                case `siteFilter_Filters_Rel` => Tuple2(tuple._1, tuple._2.::(Filter.fromNeo4jGraph(childNode.getId).get))
              }
          }
          Some(SiteFilter(Some(node.getId), tupleObj._1.asInstanceOf[AccountInfo], tupleObj._2))
        } else {
          None
        }
    }
  }
}
