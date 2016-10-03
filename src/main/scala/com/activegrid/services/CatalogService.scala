package com.activegrid.services

import com.activegrid.model._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){




  def getImages(): Future[Option[List[ImageInfo]]] = Future {

    val label: String = "ImageInfo"

    GraphDBExecutor.getEntities[ImageInfo](label)

  }

  def buildImage(image:ImageInfo):Future[Option[ImageInfo]] = Future {

    val label : String = "ImageInfo"

  GraphDBExecutor.persistEntity[ImageInfo](image, label)

  }

  def deleteImage(imageId:Long): Future[Option[String]] = Future {


    GraphDBExecutor.deleteEntity[ImageInfo](imageId)

    Some("true")

  }

  def getInstanceFlavor(siteId: Long): Future[Option[List[InstanceFlavor]]] = Future {


    val site: Option[Site]  = GraphDBExecutor.getEntity[Site](siteId)

    site match {

      case Some(a) => {
        val listOfInstances = a.instances
        Some(listOfInstances.map(instance => InstanceFlavor("CloudInstance", 100,100.10,200.10)))
      }
    }
  }

  def saveTest(site:List[Test]):Future[Option[List[Test]]] = Future {

    val label : String = "Test"

    //GraphDBExecutor.persistEntityTest(site, label)
Some(site)
  }

  def getTest(): Future[Option[List[Test]]] = Future {
    val label: String = "Test"

    GraphDBExecutor.getEntities[Test](label)

  }

  def saveImplicitTest(entity: TestImplicit): Future[Option[String]] = Future{

    import Implicits._

    entity.toGraph(entity)

    Some("success")
  }

}
