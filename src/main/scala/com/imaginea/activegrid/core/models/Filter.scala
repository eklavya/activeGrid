package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 25/10/16.
  */
case class Filter(override val id: Option[Long],
                  filterType: FilterType,
                  values: List[String]) extends BaseEntity

object Filter {
  val repository = Neo4jRepository
  val filterLabelName = "Filter"

  implicit class FilterImpl(filter: Filter) extends Neo4jRep[Filter] {
    override def toNeo4jGraph(entity: Filter): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(filterLabelName)(neo)
          if (entity.filterType != null) node.setProperty("filterType", entity.filterType.toString)
          if (entity.values.nonEmpty) node.setProperty("values", entity.values.toArray)
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Filter] = {
      Filter.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[Filter] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        Some(new Filter(Some(node.getId), FilterType.toFilteType(repository.getProperty[String](node, "filterType").get), repository.getProperty[List[String]](node, "values").get))
    }
  }
}