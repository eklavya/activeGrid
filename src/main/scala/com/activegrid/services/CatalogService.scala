package com.activegrid.services

import com.activegrid.model.{GraphDBExecutor, ImageInfo, Software}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by sampathr on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext) {

  val db = new GraphDBExecutor()

  def buildSoftware(software: Software): Future[Option[Software]] = Future {

    val softwareLabel: String = "Softwares"

    db.persistEntity[Software](software, softwareLabel)

  }


  def deleteSoftware(softwareId: Long): Future[Option[String]] = Future {


    db.deleteEntity[Software](softwareId)

    Some("true")

  }

  def getSoftwares(): Future[Option[List[Software]]] = Future {

    val softwareLabel: String = "Softwares"

    db.getEntities[Software](softwareLabel)

  }

  def getImages(): Future[Option[List[ImageInfo]]] = Future {

    val label : String = "ImageInfo"

    db.getEntities[ImageInfo](label)

  }

  def buildImage(image:ImageInfo):Future[Option[ImageInfo]] = Future {

    val label : String = "ImageInfo"

    db.persistEntity[ImageInfo](image, label)

  }

  def deleteImage(imageId:Long): Future[Option[String]] = Future {


    db.deleteEntity[ImageInfo](imageId)

    Some("true")

  }
}
