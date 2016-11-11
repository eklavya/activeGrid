/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by nagulmeeras on 25/10/16.
 */
case class Filter(override val id: Option[Long],
                  filterType: FilterType,
                  values: List[String]) extends BaseEntity

object Filter {
  val filterLabelName = "Filter"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class FilterImpl(filter: Filter) extends Neo4jRep[Filter] {
    override def toNeo4jGraph(entity: Filter): Node = {
      val map = Map("filterType" -> entity.filterType.toString, "values" -> entity.values)
      Neo4jRepository.saveEntity[Filter](filterLabelName, entity.id, map)
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Filter] = {
      Filter.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[Filter] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, filterLabelName)) {
          val map = Neo4jRepository.getProperties(node, "filterType", "values")
          Some(Filter(Some(node.getId),
            FilterType.toFilteType(map("filterType").asInstanceOf[String]),
            map("values").asInstanceOf[List[String]]))
        } else {
          None
        }
      case None => None
    }
  }
}
