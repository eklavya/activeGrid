package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.{Region, RegionUtils}
import com.amazonaws.services.ec2.model.{DescribeImagesRequest, DescribeInstancesResult, Image}
import com.amazonaws.services.ec2.{AmazonEC2, AmazonEC2Client, model}
import com.imaginea.activegrid.core.models._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.immutable.List

/**
  * Created by sampathr on 25/10/16.
  */
object AWSComputeAPI {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def getInstances(accountInfo: AccountInfo): List[Instance] = {

    val region = RegionUtils.getRegion(accountInfo.regionName)
    val aWSContextBuilder: AWSContextBuilder = AWSContextBuilder(accountInfo.accessKey, accountInfo.secretKey, accountInfo.regionName)
    val aWSCredentials1 = getAWSCredentials(aWSContextBuilder)
    val amazonEC2: AmazonEC2 = AWSInstanceHelper(aWSCredentials1, region)
    val awsInstancesResult = getAWSInstances(amazonEC2)
    val securityGroups = amazonEC2.describeSecurityGroups()
    val addresses = amazonEC2.describeAddresses()
    val imageIds = awsInstancesResult.foldLeft(List[String]())((list, awsInstance) => list.::(awsInstance.getImageId))
    val imagesMap = getImageInformation(amazonEC2, imageIds)

    awsInstancesResult.map { instance =>
      val id = Option(instance.getInstanceId)
      val instanceState = if (instance.getState != null) Option(instance.getState.getName) else None
      val keyName = Option(instance.getKeyName)
      //if (keyName != null) keyName = keyName.replaceAll("'", "")
      val platform = Option(instance.getPlatform)
      val architecture = Option(instance.getArchitecture)
      val launchTime = Some(100L)
      val privateDnsName = Option(instance.getPrivateDnsName)
      val publicDnsName = Option(instance.getPublicDnsName)
      val privateIpAddress = Option(instance.getPrivateIpAddress)
      val publicIpAddress = Option(instance.getPublicIpAddress)
      val instanceType = Option(instance.getInstanceType)
      val availabilityZone = if (instance.getPlacement != null) Option(instance.getPlacement.getAvailabilityZone) else None
      val monitoring = if (instance.getMonitoring != null) Option(instance.getMonitoring.getState) else None
      val rootDeviceType = Option(instance.getRootDeviceType)
      val nodeName = publicDnsName.getOrElse("NULL")
      val memoryInfo = Some(StorageInfo(None, 0D, AWSInstanceType.toAWSInstanceType(instance.getInstanceType).ramSize))
      val rootDiskInfo = Some(StorageInfo(None, 0D, AWSInstanceType.toAWSInstanceType(instance.getInstanceType).rootPartitionSize))
      import scala.collection.JavaConversions._
      val tags: List[KeyValueInfo] = createKeyValueInfo(instance.getTags.toList)
      val imageInfo = createImageInfo(instance.getImageId, imagesMap)
      val sshAccessInfo = createSSHAccessInfo(instance.getKeyName)

      Instance(id, nodeName, instanceState, instanceType, platform, architecture, publicDnsName, launchTime, memoryInfo, rootDiskInfo, tags, Some(imageInfo), sshAccessInfo)

    }
  }

    def createKeyValueInfo(tags: List[com.amazonaws.services.ec2.model.Tag]) : List[KeyValueInfo] = {
      tags.map { tag =>
        val key = tag.getKey
        val value = tag.getValue
        KeyValueInfo(None, key, value)
      }
    }

  def createImageInfo(imageId : String ,imagesMap : Map[String , Image]) : ImageInfo={
    logger.info(s"Image ID: $imageId")
    if(imageId.nonEmpty){

      if(imagesMap.contains(imageId)) {
        val image = imagesMap(imageId)
        ImageInfo(None,
          image.getImageId,
          image.getState,
          image.getOwnerId,
          image.getPublic,
          image.getArchitecture,
          image.getImageType,
          if(image.getPlatform != null) image.getPlatform else "",
          if(image.getImageOwnerAlias != null) image.getImageOwnerAlias else "",
          image.getName,
          if(image.getDescription != null) image.getDescription else "",
          if(image.getRootDeviceType != null) image.getRootDeviceType else "",
          image.getRootDeviceName, "")
      }else{
        ImageInfo(None,"","","",false,"","","","","","","","","")
      }
    }else{
      ImageInfo(None,"","","",false,"","","","","","","","","")
    }
  }

  def createSSHAccessInfo(keyName: String): Option[SSHAccessInfo] = {
    val node = GraphDBExecutor.getNodeByProperty("KeyPairInfo", "keyName", keyName)
    val keyPairInfo = node.flatMap { node => KeyPairInfo.fromNeo4jGraph(node.getId) }
    keyPairInfo.flatMap { info => Some(SSHAccessInfo(None, info , "", 0))}
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

    def getAWSInstances(amazonEC2: AmazonEC2) : List[model.Instance] ={
      amazonEC2.describeInstances.getReservations.toList.flatMap {
        reservation => reservation.getInstances
      }
    }

    def getImageInformation(amazonEC2: AmazonEC2 , imageIds : List[String]) : Map[String,Image]={
      val describeImagesRequest = new DescribeImagesRequest
      describeImagesRequest.setImageIds(imageIds)
      val imagesResult = amazonEC2.describeImages(describeImagesRequest)
      imagesResult.getImages.foldLeft(Map[String,Image]())((imageMap , image) => imageMap+((image.getImageId , image)))
    }

    def getImageInfoMap(imagesMap : Map[String,Image]):Map[String,ImageInfo]={
      imagesMap.foldLeft(Map[String,ImageInfo]())((imageInfoMap , image) => imageInfoMap+((image._1 ,ImageInfo.fromNeo4jGraph(image._1.toLong).get)))
    }

  }
