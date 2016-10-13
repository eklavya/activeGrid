package com.activegrid.services

import com.activegrid.model._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by shareefn on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext){

  def getImages(): Future[List[ImageInfo]] = Future {

    val label: String = "ImageInfo"

    val nodesList = GraphDBExecutor.getNodesByLabel(label)
    val imageInfo: ImageInfo = null
    val imageInfoList = nodesList.map(node => imageInfo.fromNeo4jGraph(node.getId))

    imageInfoList

  }

  def buildImage(image:ImageInfo):Future[String] = Future {


    image.toNeo4jGraph(image)

    "Built ImageInfo Successfully"

  }

  def deleteImage(imageId:Long): Future[String] = Future {


    GraphDBExecutor.deleteEntity[ImageInfo](imageId)

    "Deleted image successfully"

  }

  def getInstanceFlavor(siteId: Long): Future[List[InstanceFlavor]] = Future {

    val s : Site  = null

    val site: Site  = s.fromNeo4jGraph(siteId)

    val listOfInstances = site.instances

    listOfInstances.map(instance => InstanceFlavor(instance.instanceType, None, instance.memoryInfo.total,instance.rootDiskInfo.total))

  }

}
