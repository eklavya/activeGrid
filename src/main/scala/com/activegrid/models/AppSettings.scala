package com.activegrid.models

import org.neo4j.graphdb.{Direction, Node, Relationship, RelationshipType}

import scala.collection.mutable


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(override val id : Option[Long], settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity{}




