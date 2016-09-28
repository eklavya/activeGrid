package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


/**
  * Created by babjik on 22/9/16.
  */

class ImageInfoRepository {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "ImageInfo"
  val idField = "imageId"

  def saveImage(image: ImageInfo): ImageInfo = Neo4jRepository.saveEntity[ImageInfo](image, label)

  def getImages(): Page[ImageInfo] = {
    val images = Neo4jRepository.getEntityList[ImageInfo](label)
    logger.debug(s"images found ${images.length}" )
    new Page[ImageInfo](images)
  }


  def updateImage(imageId: String, image: ImageInfo): ImageInfo = {
    logger.debug(s"updating image ${image}")
    image
  }

  def getImageById(imageId: String) : ImageInfo = {
    logger.debug(s"finding image with id $imageId")
    val image = Neo4jRepository.getEntity[ImageInfo](label, idField, imageId)
    logger.debug(s"found val $image")
    image
  }

  def deleteImage(imageId: String): Boolean = {
    logger.debug(s"deleting image with id $imageId")
    Neo4jRepository.deleteEntity[ImageInfo](label, idField, imageId)
    true
  }
}
