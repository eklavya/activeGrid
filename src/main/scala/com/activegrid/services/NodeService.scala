package com.activegrid.services

import com.activegrid.model._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 13/10/16.
  */
class NodeService (implicit val executionContext: ExecutionContext){

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def getNode(nodeName: String): Future[Instance] = Future {

    logger.info(s"Received GET request for node - $nodeName")

    if (nodeName == "localhost") {

    }

    val instanceNode = GraphDBExecutor.getNodeByProperty("Instance", "name", nodeName)

    val instance: Instance = null

    instanceNode match {

      case Some(node) => instance.fromNeo4jGraph(Some(node.getId)).get

      case None => {

        val name = "echo node"
        val tags: List[Tuple] = List(Tuple(None, "tag", "tag"))
        val processInfo = ProcessInfo(1, 1, "init")
        Instance(name, tags, Set(processInfo))
      }

    }

  }

  def getAllNodes: List[Instance] = {

    logger.info ("Received GET request for all nodes")

    List.empty[Instance]
  }

  def getTopology: Page[Int] = {

    logger.debug("received GET request for topology")

    Page[Int](1,2,3,List(4))

  }

}

