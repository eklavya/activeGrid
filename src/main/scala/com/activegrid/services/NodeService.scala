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
      val instance = Instance(nodeName)
      instance.toNeo4jGraph(instance)
    }

    val instanceNode = GraphDBExecutor.getNodeByProperty("Instance", "name", nodeName)
    instanceNode match {
      case Some(node) => Instance.fromNeo4jGraph(Some(node.getId)).get
      case None =>
        val name = "echo node"
        val tags: List[Tuple] = List(Tuple(None, "tag", "tag"))
        val processInfo = ProcessInfo(1, 1, "init")
        Instance(name, tags, Set(processInfo))
    }

  }

  def getAllNodes: Future[Page[Instance]] = Future {

    logger.info ("Received GET request for all nodes")
    val label: String = "Instance"
    val nodesList = GraphDBExecutor.getNodesByLabel(label)
    val instanceList = nodesList.flatMap(node => Instance.fromNeo4jGraph(Some(node.getId)))

    Page[Instance](instanceList)
  }

  def getTopology: Future[Page[Instance]] = Future {

    logger.debug("received GET request for topology")

    Page[Instance](List.empty[Instance])
  }

}

