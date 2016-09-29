package com.activegrid.services

import com.activegrid.model.{GraphDBExecutor, ImageInfo, InstanceFlavor, Site}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){

  val db = new GraphDBExecutor()


  def getImages(): Future[Option[List[ImageInfo]]] = Future {

    db.getEntities[ImageInfo]

  }

  def buildImage(image:ImageInfo):Future[Option[ImageInfo]] = Future {

    val label : String = "ImageInfo"

  db.persistEntity[ImageInfo](image, label)

  }

  def deleteImage(imageId:String): Future[Option[String]] = Future {


    val paramName : String = "imageId"

    val label : String = "ImageInfo"

    db.deleteEntity[ImageInfo](label,paramName,imageId)

    Some("true")

  }

  def getInstanceFlavor(siteId: Long): Future[Option[List[InstanceFlavor]]] = Future {


    val site: Option[Site]  = db.getEntity[Site](siteId)

    site match {

      case Some(a) => {
        val listOfInstances = a.instances
        Some(listOfInstances.map(instance => InstanceFlavor("CloudInstance", 100,100.10,200.10)))
      }
    }
  }

  def saveSite(site:Site):Future[Option[Site]] = Future {

    val label : String = "Site"

    db.persistEntityTest(site, label)

  }

}
