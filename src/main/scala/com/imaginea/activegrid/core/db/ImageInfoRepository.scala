package com.imaginea.activegrid.core.db

import com.imaginea.activegrid.core.models.ImageInfo
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.slf4j.LoggerFactory


/**
  * Created by babjik on 22/9/16.
  */

class ImageInfoRepository extends AbstractRepository{
  override val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "ImageInfo"

  def saveImage(image: ImageInfo): ImageInfo = saveEntity[ImageInfo](image, label)

  def getImages(): List[ImageInfo] = getEntityList[ImageInfo](label)


  def updateImage(imageId: String, image: ImageInfo): ImageInfo = {
    logger.debug(s"updating image ${image}")
    image
  }

  def getImageById(imageId: String) : ImageInfo = {
    logger.debug(s"finding image with id $imageId")

    ImageInfo(imageId, "name", "name", "")
  }

  def deleteImage(imageId: String): ImageInfo = {
    logger.debug(s"deleting image with id $imageId")
    ImageInfo(imageId, "name", "name", "")
  }
}
