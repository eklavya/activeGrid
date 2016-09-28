package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models.{ImageInfo, ImageInfoRepository, Page}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by babjik on 22/9/16.
  */
class CatalogService (implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val imageRepository: ImageInfoRepository = new ImageInfoRepository

  def getImages: Future[Option[Page[ImageInfo]]] = Future{
    Some(imageRepository.getImages)
  }

  def createImage(image: ImageInfo): Future[Option[ImageInfo]] = Future{
    logger.debug(s"images ${image.toString}")
    Some(imageRepository.saveImage(image))
  }

  def updateImage(id: String, image: ImageInfo): Future[Option[ImageInfo]] = Future{
    logger.debug(s"images ${image.toString}")
    Some(imageRepository.updateImage(id, image))
  }

  def deleteImage(id: String): Future[Option[String]]  = Future{
    logger.debug(s"deleting images with id ${id}")
    imageRepository.deleteImage(id)
    Some("true")
  }
}
