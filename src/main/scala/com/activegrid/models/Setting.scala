package com.activegrid.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 27/09/16.
  */

case class Setting(key: String, value: String) extends BaseEntity with Neo4jRep[Setting]

