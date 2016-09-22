package com.imaginea.activegrid.core.dao

import java.util.UUID

import com.imaginea.activegrid.core.models.ImageInfo
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.slf4j.LoggerFactory


/**
  * Created by babjik on 22/9/16.
  */
class ImageInfoDAO extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "ImageInfo"

  def neo4jStoreDir = "./graphdb/activegriddb"

  def saveImage(image: ImageInfo): ImageInfo = {
    logger.debug(s"saving image ${image}")

    withTx { implicit  neo =>
      logger.debug(s"label to be created is ${label}")
      val start = createNode(label)

      logger.info(s"created node with label ${start.labels}")
    }
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
