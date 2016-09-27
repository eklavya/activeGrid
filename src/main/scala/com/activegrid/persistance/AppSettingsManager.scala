package com.activegrid.persistance


import com.activegrid.entities.AuthSettings
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.neo4j.driver.v1._
import org.neo4j.graphdb.Node
import org.neo4j.kernel.impl.api.store.RelationshipIterator.Empty


/**
  * Created by sivag on 26/9/16.
  */
class AppSettingsManager extends PersistanceManager{

  val logger = Logger(LoggerFactory.getLogger(getClass.getName));
  val label = "AppSettings"


  def persistAuthSettings(authSettings: AuthSettings): Future[AuthSettings] = Future {
    logger.info("Saving authsetting info")
    withTx {
      implicit neo =>
        val node = createNode(authSettings, label)
        logger.info(s"Writing done to ${node.getLabels} and its id is ${node.getId}")
        authSettings
    }
  }

  def getSettings(): Future[List[AuthSettings]] = Future {
    // Neo4j operations
    logger.info("Fetching app settings.")
    withTx { neo =>
      val nodes: Iterable[Node] = getAllNodesWithLabel(label)(neo)
      nodes.map(_.toCC[AuthSettings].get).toList

    }
  }

}
