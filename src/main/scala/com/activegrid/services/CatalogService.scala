package com.activegrid.services

import com.activegrid.model._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){

  def getImages(): Future[Page[ImageInfo]] = Future {

    val label: String = "ImageInfo"
    val nodesList = GraphDBExecutor.getNodesByLabel(label)
    val imageInfoList = nodesList.map(node => ImageInfo.fromNeo4jGraph(Some(node.getId)).get)

    Page[ImageInfo](imageInfoList)
  }

  def buildImage(image:ImageInfo):Future[String] = Future {

    image.toNeo4jGraph(image)

    "Built ImageInfo Successfully"
  }

  def deleteImage(imageId:Long): Future[String] = Future {

    GraphDBExecutor.deleteEntity[ImageInfo](imageId)

    "Deleted image successfully"
  }

  def getInstanceFlavor(siteId: Long): Future[Page[InstanceFlavor]] = Future {

    val site  = Site.fromNeo4jGraph(Some(siteId)).get
    val listOfInstances = site.instances
    val listOfInstanceFlavors = listOfInstances.map(instance => InstanceFlavor(instance.instanceType.get, None, instance.memoryInfo.get.total,instance.rootDiskInfo.get.total))

    Page[InstanceFlavor](listOfInstanceFlavors)
  }

}
