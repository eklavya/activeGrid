package com.activegrid.services

import com.activegrid.model.{GraphDBExecutor, ImageInfo, Page, Software}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import com.activegrid.model.CatalogImplicits._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by sampathr on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val softwareLabel: String = "SoftwaresTest"
  val imageLabel: String = "ImageInfoTest"

  def buildSoftware(software: Software): Future[Option[String]] = Future {
    software.toGraph(software)
    Some("Saved Software Successfully")
  }

  def buildImage(image: ImageInfo): Future[Option[String]] = Future {
    image.toGraph(image)
    Some("Built ImageInfo Successfully")
  }

  def deleteSoftware(softwareId: Long): Future[Option[String]] = Future {
    GraphDBExecutor.deleteEntity[Software](softwareId)
    Some("Deleted Successfully")
  }

  def getSoftwares(): Future[Option[Page[Software]]] = Future {
    val nodesList = GraphDBExecutor.getNodesByLabel(softwareLabel)
    val software: Software = null
    val softwaresList = nodesList.map(node => software.fromGraph(node.getId).get)

    Some(Page[Software](0, softwaresList.size, softwaresList.size, softwaresList))
  }

  def getImages(): Future[Option[Page[ImageInfo]]] = Future {
    val nodesList = GraphDBExecutor.getNodesByLabel(imageLabel)
    val imageInfo: ImageInfo = null
    val imageInfoList = nodesList.map(node => imageInfo.fromGraph(node.getId).get)

    Some(Page[ImageInfo](0, imageInfoList.size, imageInfoList.size, imageInfoList))
  }

  def deleteImage(imageId: Long): Future[Option[String]] = Future {
    GraphDBExecutor.deleteEntity[ImageInfo](imageId)
    Some("Deleted Successfully")
  }
}
