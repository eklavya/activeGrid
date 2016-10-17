package com.activegrid.services

import com.activegrid.model.{GraphDBExecutor, ImageInfo, Page, Software}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by sampathr on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val softwareLabel: String = "SoftwaresTest2"
  val imageLabel: String = "ImagesTest2"

  def buildSoftware(software: Software): Future[String] = Future {
    software.toNeo4jGraph(software)
    "Saved Software Successfully"
  }

  def buildImage(image: ImageInfo): Future[String] = Future {
    image.toNeo4jGraph(image)
    "Saved Image Successfully"
  }

  def deleteSoftware(softwareId: Long): Future[String] = Future {
    GraphDBExecutor.deleteEntity[Software](softwareId)
    "Deleted Successfully"
  }

  def getSoftwares(): Future[Page[Software]] = Future {
    val nodesList = GraphDBExecutor.getNodesByLabel(softwareLabel)
    val softwaresList = nodesList.map(node => Software.fromNeo4jGraph(node.getId))

    Page[Software](0, softwaresList.size, softwaresList.size, softwaresList)
  }

  def getImages(): Future[Page[ImageInfo]] = Future {
    val nodesList = GraphDBExecutor.getNodesByLabel(imageLabel)
    val imageInfoList = nodesList.map(node => ImageInfo.fromNeo4jGraph(node.getId))

    Page[ImageInfo](0, imageInfoList.size, imageInfoList.size, imageInfoList)
  }

  def deleteImage(imageId: Long): Future[String] = Future {
    GraphDBExecutor.deleteEntity[ImageInfo](imageId)
    "Deleted Successfully"
  }
}
