package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.{Region, RegionUtils}
import com.amazonaws.services.ec2.model.{DescribeImagesRequest, Image}
import com.amazonaws.services.ec2.{AmazonEC2, AmazonEC2Client, model}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.immutable.List

object AWSComputeAPI {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def getInstances(accountInfo: AccountInfo): List[Instance] = {
    val region = RegionUtils.getRegion(accountInfo.regionName)
    val aWSContextBuilder = AWSContextBuilder(accountInfo.accessKey, accountInfo.secretKey, accountInfo.regionName)
    val aWSCredentials1 = getAWSCredentials(aWSContextBuilder)
    val amazonEC2 = AWSInstanceHelper(aWSCredentials1, region)
    val awsInstancesResult = getAWSInstances(amazonEC2)
    //val securityGroups = amazonEC2.describeSecurityGroups()
    //val addresses = amazonEC2.describeAddresses()
    val imageIds = awsInstancesResult.foldLeft(List[String]())((list, awsInstance) => list.::(awsInstance.getImageId))
    val imagesMap = getImageInformation(amazonEC2, imageIds)

    //val imageInfoMap = getImageInfoMap(imagesMap )

    val instances: List[Instance] = awsInstancesResult.map {
      awsInstance =>
        val imageInfo = getImageInfo(awsInstance.getImageId, imagesMap)
        val instance = new Instance(None,
          Some(awsInstance.getInstanceId),
          awsInstance.getKeyName,
          Some(awsInstance.getState.toString),
          Option(awsInstance.getInstanceType),
          Option(awsInstance.getPlatform),
          Option(awsInstance.getArchitecture),
          Option(awsInstance.getPublicDnsName),
          Option(awsInstance.getLaunchTime.getTime),
          Some(StorageInfo(None, 0D, AWSInstanceType.toAWSInstanceType(awsInstance.getInstanceType).ramSize)),
          Some(StorageInfo(None, 0D, AWSInstanceType.toAWSInstanceType(awsInstance.getInstanceType).rootPartitionSize)),
          awsInstance.getTags.map(tag => KeyValueInfo(None, tag.getKey, tag.getValue)).toList,
          createSSHAccessInfo(awsInstance.getKeyName),
          List.empty,
          List.empty,
          Set.empty,
          Some(imageInfo),
          List.empty
        )
        logger.debug(s"Instance OBJ:$instance")
        instance
    }
    instances
  }

  private def getAWSCredentials(builder: AWSContextBuilder): AWSCredentials = {
    new AWSCredentials() {
      def getAWSAccessKeyId: String = {
        builder.accessKey
      }

      def getAWSSecretKey: String = {
        builder.secretKey
      }
    }
  }

  def AWSInstanceHelper(credentials: AWSCredentials, region: Region): AmazonEC2 = {
    val amazonEC2: AmazonEC2 = new AmazonEC2Client(credentials)
    amazonEC2.setRegion(region)
    amazonEC2
  }

  def getAWSInstances(amazonEC2: AmazonEC2): List[model.Instance] = {
    amazonEC2.describeInstances.getReservations.toList.flatMap {
      reservation => reservation.getInstances
    }
  }

  def getImageInformation(amazonEC2: AmazonEC2, imageIds: List[String]): Map[String, Image] = {
    val describeImagesRequest = new DescribeImagesRequest
    describeImagesRequest.setImageIds(imageIds)
    val imagesResult = amazonEC2.describeImages(describeImagesRequest)
    imagesResult.getImages.foldLeft(Map[String, Image]())((imageMap, image) => imageMap + ((image.getImageId, image)))
  }

  def getImageInfoMap(imagesMap: Map[String, Image]): Map[String, ImageInfo] = {
    imagesMap.foldLeft(Map[String, ImageInfo]())((imageInfoMap, image) => imageInfoMap + ((image._1, ImageInfo.fromNeo4jGraph(image._1.toLong).get)))
  }

  def getImageInfo(imageId: String, imagesMap: Map[String, Image]): ImageInfo = {
    logger.info(s"Image ID: $imageId")
    if (imageId.nonEmpty) {

      if (imagesMap.contains(imageId)) {
        val image = imagesMap(imageId)
        ImageInfo(None,
          imageId,
          image.getState,
          image.getOwnerId,
          image.getPublic,
          image.getArchitecture,
          image.getImageType,
          if (image.getPlatform != null) image.getPlatform else "",
          if (image.getImageOwnerAlias != null) image.getImageOwnerAlias else "",
          image.getName,
          if (image.getDescription != null) image.getDescription else "",
          if (image.getRootDeviceType != null) image.getRootDeviceType else "",
          image.getRootDeviceName, "")
      } else {
        ImageInfo(None, imageId, "", "", publicValue = false, "", "", "", "", "", "", "", "", "")
      }
    } else {
      ImageInfo(None, imageId, "", "", publicValue = false, "", "", "", "", "", "", "", "", "")
    }
  }

  def createSSHAccessInfo(keyName: String): Option[SSHAccessInfo] = {
    val node = GraphDBExecutor.getNodeByProperty("KeyPairInfo", "keyName", keyName)
    val keyPairInfo = node.flatMap { node => KeyPairInfo.fromNeo4jGraph(node.getId) }
    keyPairInfo.flatMap(info => Some(SSHAccessInfo(None, info, "", 0)))
  }
}
