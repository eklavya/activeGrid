package com.activegrid.services

import com.activegrid.model.{GraphDBExecutor, ImageInfo}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){

  val db = new GraphDBExecutor()
  val label : String = "ImageInfo"

  def getImages(): Future[Option[List[ImageInfo]]] = Future {

    db.getEntities[ImageInfo]

  }

  def buildImage(image:ImageInfo):Future[Option[ImageInfo]] = Future {

  db.persistEntity[ImageInfo](image, label)

  }

  def deleteImage(imageId:String): Future[Option[String]] = Future {


    val paramName : String = "imageId"

    db.deleteEntity[ImageInfo](label,paramName,imageId)

    Some("true")

  }

}
