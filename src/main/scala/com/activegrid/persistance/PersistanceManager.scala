package com.activegrid.persistance

import com.activegrid.entities.BaseEntity
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}

import scala.concurrent.Future

/**
  * Created by sivag on 27/9/16.
  */
trait PersistanceManager extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{

     def neo4jStoreDir: String = "./neo4j/activegrid2";

}
