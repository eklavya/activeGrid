package com.activegrid.services

import com.activegrid.model._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){




  def getImages(): Future[Option[List[ImageInfo]]] = Future {

    val label: String = "ImageInfo"

    val nodesList = GraphDBExecutor.getNodesByLabel(label)
    val imageInfo: ImageInfo = null
    val imageInfoList = nodesList.map(node => imageInfo.fromNeo4jGraph(node.getId).get)

    Some(imageInfoList)
  }

  def buildImage(image:ImageInfo):Future[Option[String]] = Future {


    image.toNeo4jGraph(image)

    Some("Built ImageInfo Successfully")

  }

  def deleteImage(imageId:Long): Future[Option[String]] = Future {


    GraphDBExecutor.deleteEntity[ImageInfo](imageId)

    Some("Deleted successfully")

  }

  def getInstanceFlavor(siteId: Long): Future[List[InstanceFlavor]] = Future {

    val s : Site  = null

    val site: Option[Site]  = s.fromNeo4jGraph(siteId)

    site match {

      case Some(a) => {
        val listOfInstances = a.instances
        listOfInstances.map(instance => InstanceFlavor(instance.instanceType, None, instance.memoryInfo.total,instance.rootDiskInfo.total))
      }
      case None => List()
    }
  }

}
