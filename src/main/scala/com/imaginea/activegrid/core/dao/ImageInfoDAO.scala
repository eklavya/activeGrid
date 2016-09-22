package com.imaginea.activegrid.core.dao

import com.imaginea.activegrid.core.models.ImageInfo
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


/**
  * Created by babjik on 22/9/16.
  */
class ImageInfoDAO  {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def saveImage(image: ImageInfo): ImageInfo = {
    logger.debug(s"saving image ${image}")
    image
  }

  def updateImage(imageId: String, image: ImageInfo): ImageInfo = {
    logger.debug(s"updating image ${image}")
    image
  }

  def getImages(): List[ImageInfo] = {
    logger.debug("returning all images")
    List.empty[ImageInfo]
  }

  def getImageById(imageId: String) : ImageInfo = {
    logger.debug(s"finding image with id $imageId")

    ImageInfo(imageId, "name", "name")
  }

  def deleteImage(imageId: String): ImageInfo = {
    logger.debug(s"deleting image with id $imageId")
    ImageInfo(imageId, "name", "name")
  }
}
