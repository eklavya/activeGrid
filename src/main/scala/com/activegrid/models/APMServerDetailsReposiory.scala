package com.activegrid.models

import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 14/10/16.
  */
class APMServerDetailsReposiory(implicit executionContext: ExecutionContext) {
  val logger = LoggerFactory.getLogger(getClass)

  def saveAPMServerDetails(serverDetails: APMServerDetails): Future[APMServerDetails] = Future {
    logger.debug(s"Executing $getClass :: saveAPMServerDetails")
    val serverDetailsId = serverDetails.toNeo4jGraph().getId
    val savedServerDetails = serverDetails.fromNeo4jGraph(serverDetailsId)
    logger.debug(s"Returning Saved APM Server Details : $savedServerDetails")
    savedServerDetails
  }

  def getAPMServersList(): Future[List[APMServerDetails]] = Future {
    logger.debug(s"Executing $getClass :: getAPMServerList")
    getAPMServers().toList
  }

  def getAPMServerUrl(id: Long): Future[String] = Future {
    logger.debug(s"Executing $getClass :: getAPMServerUrl ")
    val serverDetails = APMServerDetails.fromNeo4jGraph(id)
    serverDetails.serverUrl
  }

  def getAPMServerBySite(siteId: Long): Future[List[APMServerDetails]] = Future {
    logger.debug(s"Executing $getClass :: getAPMServerBySite")
    val site = Site.fromNeo4jGraph(siteId)
    val aPMServerDetails = getAPMServers()
    logger.info(s"All Sever details : $aPMServerDetails")
    val list = aPMServerDetails.filter(server => server.monitoredSite.id == site.id)
    logger.info(s"Filtered Server details : $list")
    list.toList
  }

  def getAPMServers(): mutable.MutableList[APMServerDetails] = {
    logger.debug(s"Executing $getClass :: getAPMServers")
    val nodes = APMServerDetails.getAllEntities()
    logger.debug(s"Getting all entities and size is :${nodes.size}")
    val list = mutable.MutableList.empty[APMServerDetails]
    nodes.foreach {
      node =>
        val aPMServerDetails = APMServerDetails.fromNeo4jGraph(node.getId)
        list.+=(aPMServerDetails)
    }
    logger.debug(s"Reurning list of APM Servers $list")
    list
  }
}
