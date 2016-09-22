package com.activegrid.services

import com.activegrid.graphdb.DBExecutor
import com.activegrid.model.ImageInfo

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){

  val db = new DBExecutor()

  def getImages(): Future[Option[ImageInfo]] = Future {



    Some(ImageInfo("1","jpeg","naveed"))
  }

  def buildImage(image:ImageInfo):Future[Option[ImageInfo]] =Future{

  Some(db.persistEntity(image))
    //Some(ImageInfo("1","jpeg","naveed"))

  }

}
