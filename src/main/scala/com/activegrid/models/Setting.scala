package com.activegrid.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 27/09/16.
  */

case class Setting(override val id : Option[Long] , key: String, value: String) extends BaseEntity with Neo4jRep[Setting]

