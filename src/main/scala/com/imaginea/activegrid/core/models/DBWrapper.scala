package com.imaginea.activegrid.core.models

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}

/**
  * Created by sivag on 6/10/16.
  */
trait DBWrapper extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  def neo4jStoreDir: String = "./neo4j/activegrid2"
}

