package com.activegrid.utilities

import org.neo4j.driver.v1.{AuthTokens, Driver, GraphDatabase}

/**
  * Created by nagulmeeras on 29/09/16.
  */
object Connection {
  def getConnection(): Driver ={
    val driver = GraphDatabase.driver("bolt://localhost/7687",AuthTokens.basic("neo4j","neo4j"))
    driver
  }

}
