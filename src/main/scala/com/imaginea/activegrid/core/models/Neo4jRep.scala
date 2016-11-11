/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 30/9/16.
  */

trait Neo4jRep[T] {
  def toNeo4jGraph(entity: T): Node

  def fromNeo4jGraph(id: Long): Option[T]
}

