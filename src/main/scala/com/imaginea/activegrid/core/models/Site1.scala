package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 25/10/16.
  */

case class Site1(override val id: Option[Long],
                 siteName: String,
                 instances: List[Instance],
                 reservedInstanceDetails : List[ReservedInstanceDetails],
                 filters: List[SiteFilter]) extends BaseEntity

object Site1 {
  val repository = Neo4jRepository
  val site1Label = "Site1"
  val site_Instance_Relation = "HAS_INSTANCE"
  val site_Filter_Relation = "HAS_SITE_FILTER"

  implicit class Site1Impl(site1: Site1) extends Neo4jRep[Site1] {
    override def toNeo4jGraph(entity: Site1): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(site1Label)(neo)
          if (entity.siteName.nonEmpty) node.setProperty("siteName", entity.siteName)
          if (entity.instances.nonEmpty) {
            entity.instances.foreach {
              instance =>
                val childNode = instance.toNeo4jGraph(instance)
                repository.createRelation(site_Instance_Relation, node, childNode)
            }
          }
          if (entity.filters.nonEmpty) {
            entity.filters.foreach {
              filter =>
                val childNode = filter.toNeo4jGraph(filter)
                repository.createRelation(site_Filter_Relation, node, childNode)
            }
          }
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Site1] = {
      Site1.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[Site1] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        if (repository.hasLabel(node, site1Label)) {

          val tupleObj = node.getRelationships.foldLeft(Tuple2[List[Instance], List[SiteFilter]](List.empty[Instance], List.empty[SiteFilter])) {
            (tuple, relationship) =>
              val childNode = relationship.getEndNode
              relationship.getType.name match {
                case `site_Instance_Relation` => Tuple2(tuple._1.::(Instance.fromNeo4jGraph(childNode.getId).get), tuple._2)
                case `site_Filter_Relation` => Tuple2(tuple._1, tuple._2.::(SiteFilter.fromNeo4jGraph(childNode.getId).get))
              }
          }
          Some(Site1(Some(node.getId), repository.getProperty[String](node, "siteName").get, tupleObj._1, List.empty[ReservedInstanceDetails], tupleObj._2))
        } else {
          None
        }
    }
  }
}






