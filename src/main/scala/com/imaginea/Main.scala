package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.models.{InstanceGroup, _}
import com.imaginea.activegrid.core.utils.{Constants, FileUtils, ActiveGridUtils => AGU}
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsArray, JsFalse, JsNumber, JsObject, JsString, JsTrue, JsValue, RootJsonFormat, _}

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val cachedSite = mutable.Map.empty[Long, Site1]
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))


  implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {
    override def write(obj: KeyPairStatus): JsValue = JsString(obj.name.toString)

    override def read(json: JsValue): KeyPairStatus = json match {
      case JsString(str) => KeyPairStatus.toKeyPairStatus(str)
      case _ => throw DeserializationException("Enum string expected")
    }
  }

  implicit val keyPairInfoFormat = jsonFormat(KeyPairInfo.apply, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val pageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")
  implicit val userFormat = jsonFormat(User.apply, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val pageUsersFomat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")
  implicit val sshKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")
  implicit val softwareFormat = jsonFormat(Software.apply, "id", "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
  implicit val softwarePageFormat = jsonFormat4(Page[Software])
  implicit val imageFormat = jsonFormat(ImageInfo.apply, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
  implicit val pageImageFormat = jsonFormat4(Page[ImageInfo])
  implicit val appSettingsFormat = jsonFormat(AppSettings.apply, "id", "settings", "authSettings")


  implicit object FilterTypeFormat extends RootJsonFormat[FilterType] {

    override def write(obj: FilterType): JsValue = {
      JsString(obj.filterType.toString)
    }

    override def read(json: JsValue): FilterType = {
      json match {
        case JsString(str) => FilterType.toFilteType(str)
        case _ => throw DeserializationException("Unable to deserialize Filter Type")
      }
    }
  }

  implicit object InstanceProviderFormat extends RootJsonFormat[InstanceProvider] {

    override def write(obj: InstanceProvider): JsValue = {
      JsString(obj.instanceProvider.toString)
    }

    override def read(json: JsValue): InstanceProvider = {
      json match {
        case JsString(str) => InstanceProvider.toInstanceProvider(str)
        case _ => throw DeserializationException("Unable to deserialize Filter Type")
      }
    }
  }

  implicit object ipProtocolFormat extends RootJsonFormat[IpProtocol] {

    override def write(obj: IpProtocol): JsValue = {
      JsString(obj.value.toString)
    }

    override def read(json: JsValue): IpProtocol = {
      json match {
        case JsString(str) => IpProtocol.toProtocol(str)
        case _ => throw DeserializationException("Unable to deserialize Filter Type")
      }
    }
  }

  implicit object GroupTypeFormat extends RootJsonFormat[GroupType] {
    override def write(obj: GroupType): JsValue = {
      JsString(obj.groupType.toString)
    }

    override def read(json: JsValue): GroupType = {
      json match {
        case JsString(str) => GroupType.toGroupType(str)
        case _ => throw DeserializationException("Unable to deserialize Filter Type")
      }
    }
  }


  implicit val portRangeFormat = jsonFormat(PortRange.apply, "id", "fromPort", "toPort")
  implicit val sshAccessInfoFormat = jsonFormat(SSHAccessInfo.apply, "id", "keyPair", "userName", "port")
  implicit val instanceConnectionFormat = jsonFormat(InstanceConnection.apply, "id", "sourceNodeId", "targetNodeId", "portRanges")
  implicit val processInfoFormat = jsonFormat(ProcessInfo.apply, "id", "pid", "parentPid", "name", "command", "owner", "residentBytes", "software", "softwareVersion")
  implicit val instanceUserFormat = jsonFormat(InstanceUser.apply, "id", "userName", "publicKeys")
  implicit val instanceFlavorFormat = jsonFormat(InstanceFlavor.apply, "name", "cpuCount", "memory", "rootDisk")
  implicit val pageInstFormat = jsonFormat4(Page[InstanceFlavor])
  implicit val storageInfoFormat = jsonFormat(StorageInfo.apply, "id", "used", "total")
  implicit val keyValueInfoFormat = jsonFormat(KeyValueInfo.apply, "id", "key", "value")

  implicit val ipPermissionInfoFormat = jsonFormat(IpPermissionInfo.apply, "id", "fromPort", "toPort", "ipProtocol", "groupIds", "ipRanges")
  implicit val accountInfoFormat = jsonFormat(AccountInfo.apply, "id", "accountId", "providerType", "ownerAlias", "accessKey", "secretKey", "regionName", "regions", "networkCIDR")
  implicit val snapshotInfoFormat = jsonFormat11(SnapshotInfo.apply)
  implicit val volumeInfoFormat = jsonFormat11(VolumeInfo.apply)
  implicit val instanceBlockingFormat = jsonFormat7(InstanceBlockDeviceMappingInfo.apply)
  implicit val securityGroupsFormat = jsonFormat7(SecurityGroupInfo.apply)

  implicit object instanceFormat extends RootJsonFormat[Instance] {
    override def write(i: Instance): JsValue = {
      val fieldNames = List("id", "instanceId", "name", "state", "instanceType", "platform", "architecture",
        "publicDnsName", "launchTime", "memoryInfo", "rootDiskInfo", "tags", "sshAccessInfo", "liveConnections",
        "estimatedConnections", "processes", "image", "existingUsers", "account", "availabilityZone", "privateDnsName",
        "privateIpAddress", "publicIpAddress", "elasticIp", "monitoring", "rootDeviceType", "blockDeviceMappings",
        "securityGroups", "reservedInstance", "region")
      // scalastyle:off magic.number
      val fields = new collection.mutable.ListBuffer[(String, JsValue)]
      fields ++= longToJsField(fieldNames.head, i.id)
      fields ++= stringToJsField(fieldNames(1), i.instanceId)
      fields ++= List((fieldNames(2), JsString(i.name)))
      fields ++= stringToJsField(fieldNames(3), i.state)
      fields ++= stringToJsField(fieldNames(4), i.instanceType)
      fields ++= stringToJsField(fieldNames(5), i.platform)
      fields ++= stringToJsField(fieldNames(6), i.architecture)
      fields ++= stringToJsField(fieldNames(7), i.publicDnsName)
      fields ++= longToJsField(fieldNames(8), i.launchTime)
      fields ++= objectToJsValue[StorageInfo](fieldNames(9), i.memoryInfo, storageInfoFormat)
      fields ++= objectToJsValue[StorageInfo](fieldNames(10), i.rootDiskInfo, storageInfoFormat)
      fields ++= listToJsValue[KeyValueInfo](fieldNames(11), i.tags, keyValueInfoFormat)
      fields ++= objectToJsValue[SSHAccessInfo](fieldNames(12), i.sshAccessInfo, sshAccessInfoFormat)
      fields ++= listToJsValue[InstanceConnection](fieldNames(13), i.liveConnections, instanceConnectionFormat)
      fields ++= listToJsValue[InstanceConnection](fieldNames(14), i.estimatedConnections, instanceConnectionFormat)
      fields ++= setToJsValue[ProcessInfo](fieldNames(15), i.processes, processInfoFormat)
      fields ++= objectToJsValue[ImageInfo](fieldNames(16), i.image, imageFormat)
      fields ++= listToJsValue[InstanceUser](fieldNames(17), i.existingUsers, instanceUserFormat)
      fields ++= objectToJsValue[AccountInfo](fieldNames(18), i.account, accountInfoFormat)
      fields ++= stringToJsField(fieldNames(19), i.availabilityZone)
      fields ++= stringToJsField(fieldNames(20), i.privateDnsName)
      fields ++= stringToJsField(fieldNames(21), i.privateIpAddress)
      fields ++= stringToJsField(fieldNames(22), i.publicIpAddress)
      fields ++= stringToJsField(fieldNames(23), i.elasticIP)
      fields ++= stringToJsField(fieldNames(24), i.monitoring)
      fields ++= stringToJsField(fieldNames(25), i.rootDeviceType)
      fields ++= listToJsValue[InstanceBlockDeviceMappingInfo](fieldNames(26), i.blockDeviceMappings, instanceBlockingFormat)
      fields ++= listToJsValue[SecurityGroupInfo](fieldNames(27), i.securityGroups, securityGroupsFormat)
      fields ++= List((fieldNames(28), JsBoolean(i.reservedInstance)))
      fields ++= stringToJsField(fieldNames(29), i.region)
      // scalastyle:on magic.number
      JsObject(fields: _*)
    }

    override def read(json: JsValue): Instance = {
      json match {
        case JsObject(map) =>
          logger.info(s"Js Map $map")
          Instance(
            getProperty[Long](map, "id"),
            getProperty[String](map, "instanceId"),
            getProperty[String](map, "name").get,
            getProperty[String](map, "state"),
            getProperty[String](map, "instanceType"),
            getProperty[String](map, "platform"),
            getProperty[String](map, "architecture"),
            getProperty[String](map, "publicDnsName"),
            getProperty[Long](map, "launchTime"),
            getProperty[String](map, "memoryInfo").map(memoryInfo => storageInfoFormat.read(memoryInfo.asInstanceOf[JsValue])),
            getProperty[String](map, "rootDiskInfo").map(rootDiskInfo => storageInfoFormat.read(rootDiskInfo.asInstanceOf[JsValue])),
            getObjectsFromJson[KeyValueInfo](map, "keyValueInfo", keyValueInfoFormat),
            getProperty[String](map, "sshAccessInfo").map(sshAccessInfo => sshAccessInfoFormat.read(sshAccessInfo.asInstanceOf[JsValue])),
            getObjectsFromJson[InstanceConnection](map, "liveConnections", instanceConnectionFormat),
            getObjectsFromJson[InstanceConnection](map, "estimatedConnections", instanceConnectionFormat),
            getObjectsFromJson[ProcessInfo](map, "processes", processInfoFormat).toSet,
            getProperty[String](map, "imageInfo").map(imageInfo => imageFormat.read(imageInfo.asInstanceOf[JsValue])),
            getObjectsFromJson[InstanceUser](map, "existingUsers", instanceUserFormat),
            getProperty[String](map, "account").map(accountInfo => accountInfoFormat.read(accountInfo.asInstanceOf[JsValue])),
            getProperty[String](map, "availabilityZone"),
            getProperty[String](map, "privateDnsName"),
            getProperty[String](map, "privateIpAddress"),
            getProperty[String](map, "publicIpAddress"),
            getProperty[String](map, "elasticIp"),
            getProperty[String](map, "monitoring"),
            getProperty[String](map, "rootDeviceType"),
            getObjectsFromJson[InstanceBlockDeviceMappingInfo](map, "blockDeviceMappings", instanceBlockingFormat),
            getObjectsFromJson[SecurityGroupInfo](map, "securityGroups", securityGroupsFormat),
            getProperty[Boolean](map, "reservedInstance").get,
            getProperty[String](map, "region")
          )
        case _ => logger.debug("Not a JsObject")
          Instance("Test")
      }
    }

    def stringToJsField(fieldName: String, fieldValue: Option[String], rest: List[JsField] = Nil): List[(String, JsValue)] = {
      fieldValue match {
        case Some(x) => (fieldName, JsString(x)) :: rest
        case None => rest
      }
    }

    def getProperty[T: Manifest](propertyMap: Map[String, JsValue], property: String): Option[T] = {
      if (propertyMap.contains(property)) {
        propertyMap(property) match {
          case JsString(str) => Some(str.asInstanceOf[T])
          case JsNumber(str) => Some(str.asInstanceOf[T])
          case JsFalse => Some(false.asInstanceOf[T])
          case JsTrue => Some(true.asInstanceOf[T])
          case _ => None
        }
      } else {
        None
      }
    }

    def getObjectsFromJson[T: Manifest](propertyMap: Map[String, JsValue], property: String, formateObject: RootJsonFormat[T]): List[T] = {
      if (propertyMap.contains(property)) {
        val listOfObjs = propertyMap(property).asInstanceOf[JsArray]
        listOfObjs.elements.foldLeft(List[T]()) {
          (list, jsString) =>
            formateObject.read(jsString) :: list
        }
      } else {
        List.empty[T]
      }
    }

    def longToJsField(fieldName: String, fieldValue: Option[Long], rest: List[JsField] = Nil): List[(String, JsValue)] = {
      fieldValue match {
        case Some(x) => (fieldName, JsNumber(x)) :: rest
        case None => rest
      }
    }

    def objectToJsValue[T](fieldName: String, obj: Option[T], jsonFormat: RootJsonFormat[T], rest: List[JsField] = Nil): List[(String, JsValue)] = {
      obj match {
        case Some(x) => (fieldName, jsonFormat.write(x.asInstanceOf[T])) :: rest
        case None => rest
      }
    }

    def listToJsValue[T](fieldName: String, objList: List[T], jsonFormat: RootJsonFormat[T], rest: List[JsField] = Nil): List[(String, JsValue)] = {
      objList.map { obj => (fieldName, jsonFormat.write(obj))
      }
    }

    def setToJsValue[T](fieldName: String, objList: Set[T], jsonFormat: RootJsonFormat[T], rest: List[JsField] = Nil): List[(String, JsValue)] = {
      objList.map { obj => (fieldName, jsonFormat.write(obj))
      }.toList
    }
  }

  implicit val PageInstanceFormat = jsonFormat4(Page[Instance])
  implicit val siteFormat = jsonFormat(Site.apply, "id", "instances", "siteName", "groupBy")
  implicit val appSettings = jsonFormat(ApplicationSettings.apply, "id", "settings", "authSettings")

  implicit val resourceACLFormat = jsonFormat(ResourceACL.apply, "id", "resources", "permission", "resourceIds")
  implicit val userGroupFormat = jsonFormat(UserGroup.apply, "id", "name", "users", "accesses")
  implicit val pageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")
  implicit val reservedInstanceDetailsFormat = jsonFormat(ReservedInstanceDetails.apply, "id", "instanceType", "reservedInstancesId", "availabilityZone", "tenancy", "offeringType", "productDescription", "count")
  implicit val siteACLFormat = jsonFormat(SiteACL.apply, "id", "name", "site", "instances", "groups")
  implicit val pageSiteACLFormat = jsonFormat(Page[SiteACL], "startIndex", "count", "totalObjects", "objects")
  implicit val instanceGroupFormat = jsonFormat(InstanceGroup.apply, "id", "groupType", "name", "instances")

  implicit object apmProviderFormat extends RootJsonFormat[APMProvider] {
    override def write(obj: APMProvider): JsValue = {
      logger.info(s"Writing APMProvider json : ${obj.provider.toString}")
      JsString(obj.provider.toString)
    }

    override def read(json: JsValue): APMProvider = {
      logger.info(s"Reading json value : ${json.toString}")
      json match {
        case JsString(str) => APMProvider.toProvider(str)
        case _ => throw DeserializationException("Unable to deserialize the Provider data")
      }
    }
  }

  implicit object ConditionFormat extends RootJsonFormat[Condition] {
    override def write(obj: Condition): JsValue = {
      logger.info(s"Writing Condition json : ${obj.condition.toString}")
      JsString(obj.condition.toString)
    }

    override def read(json: JsValue): Condition = {
      logger.info(s"Reading json value : ${json.toString}")
      json match {
        case JsString(str) => Condition.toCondition(str)
        case _ => throw DeserializationException("Unable to deserialize the Condition data")
      }
    }
  }

  implicit val filterFormat = jsonFormat(Filter.apply, "id", "filterType", "values")
  implicit val siteFilterFormat = jsonFormat(SiteFilter.apply, "id", "accountInfo", "filters")
  implicit val loadBalancerFormat = jsonFormat(LoadBalancer.apply, "id", "name", "vpcId", "region", "instanceIds", "availabilityZones")
  implicit val pageLoadBalancerFormat = jsonFormat4(Page[LoadBalancer])
  implicit val scalingGroupFormat = jsonFormat(ScalingGroup.apply, "id", "name", "launchConfigurationName", "status", "availabilityZones", "instanceIds", "loadBalancerNames", "tags", "desiredCapacity", "maxCapacity", "minCapacity")
  implicit val apmServerDetailsFormat = jsonFormat(APMServerDetails.apply, "id", "name", "serverUrl", "monitoredSite", "provider", "headers")
  implicit val site1Format = jsonFormat(Site1.apply, "id", "siteName", "instances", "reservedInstanceDetails", "filters", "loadBalancers", "scalingGroups", "groupsList")
  implicit object SiteDeltaStatusFormat extends RootJsonFormat[SiteDeltaStatus] {
    override def write(obj: SiteDeltaStatus): JsValue = {
      JsString(obj.deltaStatus.toString)
    }

    override def read(json: JsValue): SiteDeltaStatus = {
      json match {
        case JsString(str) => SiteDeltaStatus.toDeltaStatus(str)
        case _ => throw DeserializationException("Unable to deserialize SiteDeltaStatus")
      }
    }
  }
  implicit val siteDeltaFormat = jsonFormat(SiteDelta.apply, "siteId", "deltaStatus", "addedInstances", "deletedInstances")

  def appSettingServiceRoutes = post {
    path("appsettings") {
      entity(as[AppSettings]) {
        appsetting =>
          onComplete(Future {
            appsetting.toNeo4jGraph(appsetting)
          }) {
            case Success(response) => complete(StatusCodes.OK, "Done")
            case Failure(exception) =>
              logger.error(s"Unable to save App Settings ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to save App Settings")
          }
      }
    }
  } ~ get {
    path("config") {
      val appSettings = Future {
        AppSettings.getAppSettingNode.flatMap(node => AppSettings.fromNeo4jGraph(node.getId))
      }
      onComplete(appSettings) {
        case Success(response) =>
          response match {
            case Some(appsettings) => complete(StatusCodes.OK, appsettings)
            case None => complete(StatusCodes.BadRequest, "Unable to get the App Settings")
          }
        case Failure(exception) =>
          logger.error(s"Unable to get App Settings ${exception.getMessage}", exception)
          complete(StatusCodes.BadRequest, "Unable to get App Settings")
      }
    }
  } ~ put {
    path(PathMatchers.separateOnSlashes("config/settings")) {
      entity(as[Map[String, String]]) {
        setting =>
          val response = Future {
            AppSettings.updateAppSettings(setting, "Has_Settings")
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, "Done")
            case Failure(exception) =>
              logger.error(s"Unable to save the settings ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to save the settings")
          }
      }
    }
  } ~ put {
    path(PathMatchers.separateOnSlashes("config/settings/auth")) {
      entity(as[Map[String, String]]) {
        setting =>
          val response = Future {
            AppSettings.updateAppSettings(setting, "Has_AuthSettings")
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, "Done")
            case Failure(exception) =>
              logger.error(s"Unable to save the Auth settings ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to save the Auth settings")
          }
      }
    }
  } ~ path(PathMatchers.separateOnSlashes("config/settings")) {
    get {
      val appSettings = Future {
        AppSettings.getAppSettingNode.flatMap(node => AppSettings.fromNeo4jGraph(node.getId))
      }
      onComplete(appSettings) {
        case Success(response) => complete(StatusCodes.OK, response)
        case Failure(exception) =>
          logger.error(s"Unable to fetch settings ${exception.getMessage}", exception)
          complete(StatusCodes.BadRequest, "Unable to fetch the settings")
      }
    }
  } ~ path(PathMatchers.separateOnSlashes("config/settings")) {
    delete {
      entity(as[List[String]]) { list =>
        val isDeleted = Future {
          AppSettings.deleteSettings(list, "Has_Settings")
        }
        onComplete(isDeleted) {
          case Success(response) => complete(StatusCodes.OK, "Done")
          case Failure(exception) =>
            logger.error(s"Unable to delete settings ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to delete  settings")
        }
      }
    }
  }

  def apmServiceRoutes = path(PathMatchers.separateOnSlashes("apm")) // scalastyle:ignore
  {
    post {
      entity(as[APMServerDetails]) { apmServerDetails =>
        logger.debug(s"Executing $getClass :: saveAPMServerDetails")

        val serverDetails = Future {
          val serverDetailsEnity = apmServerDetails.toNeo4jGraph(apmServerDetails)
          apmServerDetails.fromNeo4jGraph(serverDetailsEnity.getId)
        }
        onComplete(serverDetails) {
          case Success(response) =>
            response match {
              case Some(details) => complete(StatusCodes.OK, response)
              case None => complete(StatusCodes.BadRequest, "Unable to Save Server Details")
            }
          case Failure(exception) =>
            logger.error(s"Unable to save the APM Server Details ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to save the Server details")
        }
      }
    }
  } ~ path(PathMatchers.separateOnSlashes("apm")) {
    get {
      val serverDetailsList = Future {
        getAPMServers.toList
      }
      onComplete(serverDetailsList) {
        case Success(response) => complete(StatusCodes.OK, response)
        case Failure(exception) =>
          logger.error(s"Unable get the APM Server Details ${exception.getMessage}", exception)
          complete(StatusCodes.BadRequest, "Unable get the APM server details")
      }
    }
  } ~ path("apm" / LongNumber / "url") {
    serverId =>
      get {
        logger.info(s"getting into request context : $serverId")
        val serverDetailsList = Future {
          APMServerDetails.fromNeo4jGraph(serverId).map(serverDetails => serverDetails.serverUrl)
        }
        onComplete(serverDetailsList) {
          case Success(response) =>
            response match {
              case Some(detailsList) => complete(StatusCodes.OK, detailsList)
              case None => complete(StatusCodes.BadRequest, s"Unable to get URL with given ID : $serverId")
            }
          case Failure(exception) =>
            logger.error(s"Unable to get the APM Server Url ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get the APM Server Url")
        }
      }
  } ~ path("apm" / IntNumber) {
    siteId => get {

      val serverDetails = Future {
        Site.fromNeo4jGraph(siteId).map { site =>
          val aPMServerDetails = getAPMServers
          logger.info(s"All Sever details : $aPMServerDetails")
          val list = aPMServerDetails.filter(server => {
            if (server.monitoredSite.nonEmpty) server.monitoredSite.get.id == site.id else false
          })
          logger.info(s"Filtered Server details : $list")
          list.toList
        }
      }
      onComplete(serverDetails) {
        case Success(response) =>
          response match {
            case Some(details) => complete(StatusCodes.OK, details)
            case None => complete(StatusCodes.BadRequest, s"Unable to get APMServer Details with given Site ID : $siteId")
          }
        case Failure(exception) =>
          logger.error(s"Unable to get the APM Server Details : $exception")
          complete(StatusCodes.BadRequest, s"Unable to get the APM Server Details with Site Id :$siteId")
      }
    }
  }

  def userRoute: Route = pathPrefix("users") {
    path("groups" / LongNumber) { id =>
      get {
        val result = Future {
          UserGroup.fromNeo4jGraph(id)
        }
        onComplete(result) {
          case Success(mayBeUserGroup) =>
            mayBeUserGroup match {
              case Some(userGroup) => complete(StatusCodes.OK, userGroup)
              case None => complete(StatusCodes.BadRequest, s"Failed to get user group with id $id")
            }
          case Failure(ex) =>
            logger.error(s"Failed to get user group, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to get user group, Message: ${ex.getMessage}")
        }
      } ~
        delete {
          val result = Future {
            Neo4jRepository.deleteEntity(id)
          }
          onComplete(result) {
            case Success(status) => complete(StatusCodes.OK, "Deleted succesfully")
            case Failure(ex) =>
              logger.error(s"Failed to delete user group, Message: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Failed to delete user group, Message: ${ex.getMessage}")
          }
        }
    } ~
      path("groups") {
        get {
          val result = Future {
            val nodeList = Neo4jRepository.getNodesByLabel(UserGroup.label)
            val listOfUserGroups = nodeList.flatMap(node => UserGroup.fromNeo4jGraph(node.getId))
            Page[UserGroup](listOfUserGroups)
          }
          onComplete(result) {
            case Success(page) => complete(StatusCodes.OK, page)
            case Failure(ex) =>
              logger.error(s"Failed to get users, Message: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Failed to get users")
          }
        } ~ post {
          entity(as[UserGroup]) { userGroup =>
            val result = Future {
              userGroup.toNeo4jGraph(userGroup)
            }
            onComplete(result) {
              case Success(status) => complete(StatusCodes.OK, "User group saved  Successfully")
              case Failure(ex) =>
                logger.error(s"Failed save user group, Message: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Failed save user group")
            }
          }
        }
      }
  } ~
    pathPrefix("users") {
      path("access" / LongNumber) { id =>
        get {
          val result = Future {
            SiteACL.fromNeo4jGraph(id)
          }
          onComplete(result) {
            case Success(mayBeSiteACL) =>
              mayBeSiteACL match {
                case Some(userGroup) => complete(StatusCodes.OK, userGroup)
                case None => complete(StatusCodes.BadRequest, s"Failed to get user access with id $id")
              }
            case Failure(ex) =>
              logger.error(s"Failed to get user access, Message: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Failed to get access, Message: ${ex.getMessage}")
          }
        } ~
          delete {
            val result = Future {
              Neo4jRepository.deleteEntity(id)
            }
            onComplete(result) {
              case Success(status) => complete(StatusCodes.OK, "Deleted succesfully")
              case Failure(ex) =>
                logger.error(s"Failed to delete user access, Message: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Failed to delete user access, Message: ${ex.getMessage}")
            }
          }
      } ~
        path("access") {
          get {
            val result = Future {
              val nodeList = Neo4jRepository.getNodesByLabel(SiteACL.label)
              val listOfSiteACL = nodeList.flatMap(node => SiteACL.fromNeo4jGraph(node.getId))
              Page[SiteACL](listOfSiteACL)
            }
            onComplete(result) {
              case Success(page) => complete(StatusCodes.OK, page)
              case Failure(ex) =>
                logger.error(s"Failed to get user access, Message: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Failed to get user access")
            }
          } ~ post {
            entity(as[SiteACL]) { siteACL =>
              val result = Future {
                siteACL.toNeo4jGraph(siteACL)
              }
              onComplete(result) {
                case Success(status) => complete(StatusCodes.OK, "Site access saved  Successfully")
                case Failure(ex) =>
                  logger.error(s"Failed save Site access, Message: ${ex.getMessage}", ex)
                  complete(StatusCodes.BadRequest, s"Failed save Site access")
              }
            }
          }
        }
    } ~
    pathPrefix("users" / LongNumber) { userId =>
      pathPrefix("keys") {
        pathPrefix(LongNumber) { keyId =>
          get {
            val key = Future {
              getKeyById(userId, keyId)
            }
            onComplete(key) {
              case Success(response) =>
                response match {
                  case Some(keyPairInfo) => complete(StatusCodes.OK, keyPairInfo)
                  case None => complete(StatusCodes.BadRequest, "Unable to get the key")
                }
              case Failure(ex) =>
                logger.error(s"Unable to get the key, Reason: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Unable to get the key")
            }

          } ~ delete {
            val resposne = Future {
              logger.debug(s"Deleting Key[$keyId] of User[$userId] ")
              getKeyById(userId, keyId).flatMap(key => {
                Neo4jRepository.deleteChildNode(keyId)
              })
            }
            onComplete(resposne) {
              case Success(result) => complete(StatusCodes.OK, "Deleted Successfully")
              case Failure(ex) =>
                logger.error(s"Failed delete, Message: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Failed to delete")
            }
          }
        } ~ get {
          val result = Future {
            User.fromNeo4jGraph(userId) match {
              case Some(user) => Page[KeyPairInfo](user.publicKeys)
              case None => Page[KeyPairInfo](List.empty[KeyPairInfo])
            }
          }
          onComplete(result) {
            case Success(page) => complete(StatusCodes.OK, page)
            case Failure(ex) =>
              logger.error(s"Failed get Users, Message: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Failed get Users")
          }
        } ~ post {
          entity(as[SSHKeyContentInfo]) { sshKeyInfo =>
            val result = Future {

              FileUtils.createDirectories(UserUtils.getKeyDirPath(userId))

              val resultKeys = sshKeyInfo.keyMaterials.map { case (keyName: String, keyMaterial: String) =>
                logger.debug(s" ($keyName  --> ($keyMaterial))")
                val filePath: String = UserUtils.getKeyFilePath(userId, keyName)
                FileUtils.saveContentToFile(filePath, keyMaterial)

                val keyPairInfo = KeyPairInfo(keyName, keyMaterial, Some(filePath), UploadedKeyPair)
                logger.debug(s" new Key Pair Info $keyPairInfo")
                UserUtils.addKeyPair(userId, keyPairInfo)
                keyPairInfo
              }

              Page(resultKeys.toList)
            }
            onComplete(result) {
              case Success(page) => complete(StatusCodes.OK, page)
              case Failure(ex) =>
                logger.error(s"Failed to add keys, Message: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Failed to add keys")
            }
          }
        }
      } ~ get {
        val result = Future {
          User.fromNeo4jGraph(userId)
        }
        onComplete(result) {
          case Success(mayBeUser) =>
            mayBeUser match {
              case Some(user) => complete(StatusCodes.OK, user)
              case None => complete(StatusCodes.BadRequest, s"Failed to get user with id $userId")
            }
          case Failure(ex) =>
            logger.error(s"Failed to get user, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to get user, Message: ${ex.getMessage}")
        }
      } ~ delete {
        val result = Future {
          Neo4jRepository.deleteEntity(userId)
        }
        onComplete(result) {
          case Success(status) => complete(StatusCodes.OK, "Deleted succesfully")
          case Failure(ex) =>
            logger.error(s"Failed to delete user, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to delete user, Message: ${ex.getMessage}")
        }
      }
    } ~ pathPrefix("users") {
    get {
      pathPrefix(Segment / "keys") { userName =>
        val result = Future {
          logger.debug(s"Searching Users with name $userName")
          val maybeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("User", "username", userName)

          logger.debug(s" May be node $maybeNode")

          maybeNode match {
            case None => Page(List.empty[KeyPairInfo])
            case Some(node) =>
              User.fromNeo4jGraph(node.getId) match {
                case Some(user) => Page[KeyPairInfo](user.publicKeys)
                case None => Page[KeyPairInfo](List.empty[KeyPairInfo])
              }
          }
        }
        onComplete(result) {
          case Success(page) => complete(StatusCodes.OK, page)
          case Failure(ex) =>
            logger.error(s"Failed to get keys, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to get keys")
        }
      }
    } ~ get {
      val result = Future {
        val nodeList = Neo4jRepository.getNodesByLabel("User")
        val listOfUsers = nodeList.flatMap(node => User.fromNeo4jGraph(node.getId))

        Page[User](listOfUsers)
      }
      onComplete(result) {
        case Success(page) => complete(StatusCodes.OK, page)
        case Failure(ex) =>
          logger.error(s"Failed to get users, Message: ${ex.getMessage}", ex)
          complete(StatusCodes.BadRequest, s"Failed to get users")
      }
    } ~ post {
      entity(as[User]) { user =>
        val result = Future {
          user.toNeo4jGraph(user)
        }
        onComplete(result) {
          case Success(status) => complete(StatusCodes.OK, "Successfully saved user")
          case Failure(ex) =>
            logger.error(s"Failed save user, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed save user")
        }
      }
    }
  }


  //KeyPair Serivce
  def keyPairRoute: Route = pathPrefix("keypairs") {
    pathPrefix(LongNumber) { keyId =>
      get {
        val result = Future {
          Neo4jRepository.findNodeByLabelAndId("KeyPairInfo", keyId).flatMap(node => KeyPairInfo.fromNeo4jGraph(node.getId))
        }
        onComplete(result) {
          case Success(mayBekey) =>
            mayBekey match {
              case Some(key) => complete(StatusCodes.OK, key)
              case None => complete(StatusCodes.BadRequest, s"failed to get key pair for id $keyId")
            }
          case Failure(ex) =>
            logger.error(s"Failed to get Key Pair, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to get Key Pair")
        }
      } ~ delete {
        val result = Future {
          Neo4jRepository.deleteChildNode(keyId)
        }
        onComplete(result) {
          case Success(key) => complete(StatusCodes.OK, "Deleted Successfully")
          case Failure(ex) =>
            logger.error(s"Failed to delete Key Pair, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to delete Key Pair")
        }
      }
    } ~ get {
      val result = Future {
        val nodeList = Neo4jRepository.getNodesByLabel("KeyPairInfo")
        logger.debug(s"nodeList $nodeList")
        val listOfKeys = nodeList.flatMap(node => KeyPairInfo.fromNeo4jGraph(node.getId))
        Page[KeyPairInfo](listOfKeys)
      }
      onComplete(result) {
        case Success(page) => complete(StatusCodes.OK, page)
        case Failure(ex) =>
          logger.error(s"Failed to get Keys, Message: ${ex.getMessage}", ex)
          complete(StatusCodes.BadRequest, s"Failed to get Keys")
      }
    } ~ put {
      entity(as[Multipart.FormData]) { formData =>
        val result = Future {

          val dataMap = formData.asInstanceOf[FormData.Strict].strictParts.foldLeft(Map[String, String]())((accum, strict) => {
            val name = strict.getName()
            val value = strict.entity.getData().decodeString("UTF-8")
            val mayBeFile = strict.filename
            logger.debug(s"--- $name  -- $value -- $mayBeFile")

            mayBeFile match {
              case Some(fileName) => accum + ((name, value))
              case None =>
                if (name.equalsIgnoreCase("userName") || name.equalsIgnoreCase("passPhase"))
                  accum + ((name, value))
                else
                  accum
            }
          })

          val sshKeyContentInfo: SSHKeyContentInfo = SSHKeyContentInfo(dataMap)
          logger.debug(s"ssh info   - $sshKeyContentInfo")
          logger.debug(s"Data Map --- $dataMap")
          val userNameLabel = "userName"
          val passPhaseLabel = "passPhase"
          val addedKeyPairs: List[KeyPairInfo] = dataMap.map { case (keyName, keyMaterial) =>
            if (!userNameLabel.equalsIgnoreCase(keyName) && !passPhaseLabel.equalsIgnoreCase(keyName)) {
              val keyPairInfo = getOrCreateKeyPair(keyName, keyMaterial, None, UploadedKeyPair, dataMap.get(userNameLabel), dataMap.get(passPhaseLabel))
              val mayBeNode = saveKeyPair(keyPairInfo)
              mayBeNode match {
                case Some(key) => key
                case _ => // do nothing
              }
            }
          }.toList.collect { case x: KeyPairInfo => x }

          Page[KeyPairInfo](addedKeyPairs)
        }
        onComplete(result) {
          case Success(page) => complete(StatusCodes.OK, page)
          case Failure(ex) =>
            logger.error(s"Failed to update Keys, Message: ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Failed to update Keys")
        }
      }
    }
  }


  def catalogRoutes : Route = pathPrefix("catalog") {
    path("images" / "view") {
      get {
        val getImages: Future[Page[ImageInfo]] = Future {
          val imageLabel: String = "ImageInfo"
          val nodesList = Neo4jRepository.getNodesByLabel(imageLabel)
          val imageInfoList = nodesList.flatMap(node => ImageInfo.fromNeo4jGraph(node.getId))

          Page[ImageInfo](imageInfoList)
        }

        onComplete(getImages) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to Retrieve ImageInfo List. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to Retrieve ImageInfo List.")
        }
      }
    } ~ path("images") {
      put {
        entity(as[ImageInfo]) { image =>
          val buildImage = Future {
            image.toNeo4jGraph(image)
            "Successfully added ImageInfo"
          }
          onComplete(buildImage) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to Save Image. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to Save Image.")
          }
        }
      }
    } ~ path("images" / LongNumber) { imageId =>
      delete {
        val deleteImages = Future {
          Neo4jRepository.deleteEntity(imageId)
        }

        onComplete(deleteImages) {
          case Success(successResponse) => complete(StatusCodes.OK, "Successfully deleted ImageInfo")
          case Failure(exception) =>
            logger.error(s"Unable to Delete Image. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to Delete Image.")
        }
      }
    } ~ path("softwares") {
      put {
        entity(as[Software]) { software =>
          val buildSoftware = Future {
            software.toNeo4jGraph(software)
            "Saved Software Successfully"
          }
          onComplete(buildSoftware) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to Save Software. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to Save Software.")
          }
        }
      }
    } ~ path("softwares" / LongNumber) { softwareId =>
      delete {
        val deleteSoftware = Future {
          Neo4jRepository.deleteEntity(softwareId)
          "Deleted Successfully"
        }

        onComplete(deleteSoftware) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to Delete Software. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to Delete Software.")
        }
      }
    } ~ path("softwares") {
      get {
        val getSoftwares = Future {
          val softwareLabel: String = "SoftwaresTest2"
          val nodesList = Neo4jRepository.getNodesByLabel(softwareLabel)
          val softwaresList = nodesList.flatMap(node => Software.fromNeo4jGraph(node.getId))
          Page[Software](softwaresList)
        }
        onComplete(getSoftwares) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to Retrieve Softwares List. Failed with :  ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to Retrieve Softwares List.")
        }
      }
    } ~ path("instanceTypes") {
      get {
        parameter("siteId".as[Int]) { siteId =>
          val listOfInstanceFlavors = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) =>
                val listOfInstances = site.instances
                val listOfInstanceFlavors = listOfInstances.map { instance =>
                  InstanceFlavor(instance.instanceType.get, None, instance.memoryInfo.get.total, instance.rootDiskInfo.get.total)
                }
                Page[InstanceFlavor](listOfInstanceFlavors)

              case None =>
                logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
                Page[InstanceFlavor](List.empty[InstanceFlavor])
            }
          }
          onComplete(listOfInstanceFlavors) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get List; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to get List of Instance Flavors")
          }
        }
      }
    }
  }

  def nodeRoutes : Route = pathPrefix("node") {
    path("list") {
      get {
        val listOfAllInstanceNodes = Future {
          logger.info("Received GET request for all nodes")
          val label: String = "Instance"
          val nodesList = Neo4jRepository.getNodesByLabel(label)
          val instanceList = nodesList.flatMap(node => Instance.fromNeo4jGraph(node.getId))
          Page[Instance](instanceList)
        }
        onComplete(listOfAllInstanceNodes) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(ex) =>
            logger.error(s"Unable to get Instance nodes; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Unable to get Instance nodes")
        }
      }
    } ~ path("topology") {
      get {
        val topology = Future {
          logger.debug("received GET request for topology")
          Page[Instance](List.empty[Instance])
        }
        onComplete(topology) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(ex) =>
            logger.error(s"Unable to get Instance nodes; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Unable to get Instance nodes")
        }
      }
    } ~ path(Segment) { name =>
      get {
        val nodeInstance = Future {
          logger.info(s"Received GET request for node - $name")
          if (name == "localhost") {
            val instance = Instance(name)
            instance.toNeo4jGraph(instance)
          }
          val instanceNode = Neo4jRepository.getNodeByProperty("Instance", "name", name)
          instanceNode match {
            case Some(node) => Instance.fromNeo4jGraph(node.getId).get
            case None =>
              val name = "echo node"
              val tags: List[KeyValueInfo] = List(KeyValueInfo(None, "tag", "tag"))
              val processInfo = ProcessInfo(1, 1, "init")
              Instance(name, tags, Set(processInfo))
          }
        }
        onComplete(nodeInstance) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(ex) =>
            logger.error(s"Unable to get Instance with name $name; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, s"Unable to get Instance with name $name")
        }
      }
    }
  }

  val appsettingRoutes : Route = pathPrefix("config") {
    path("ApplicationSettings") {
      post {
        entity(as[ApplicationSettings]) { appSettings =>
          val maybeAdded = Future {
            appSettings.toNeo4jGraph(appSettings)
          }
          onComplete(maybeAdded) {
            case Success(save) => complete(StatusCodes.OK, "Settings saved successfully")
            case Failure(ex) =>
              logger.error("Error while save settings", ex)
              complete(StatusCodes.InternalServerError, "These is problem while processing request")
          }

        }
      }
    }
  } ~ pathPrefix("config") {
    path("ApplicationSettings") {
      get {
        val allSettings = Future {
          AppSettingsNeo4jWrapper.fromNeo4jGraph(0L)
        }
        onComplete(allSettings) {
          case Success(settings) =>
            complete(StatusCodes.OK, settings)
          case Failure(ex) =>
            logger.error("Failed to get settings", ex)
            complete("Failed to get settings")
        }
      }
    }
  } ~ pathPrefix("config") {
    path("ApplicationSettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated = AppSettingsNeo4jWrapper.updateSettings(appSettings, "GENERAL_SETTINGS")
          onComplete(maybeUpdated) {
            case Success(update) => update.status match {
              case true => complete(StatusCodes.OK, "Updated successfully")
              case false => complete(StatusCodes.OK, "Updated failed,,Retry!!")
            }
            case Failure(ex) =>
              ex match {
                case aie: IllegalArgumentException =>
                  logger.error("Update operation failed", ex)
                  complete(StatusCodes.OK, "Failed to update settings")
                case _ =>
                  logger.error("Update operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }

  } ~ pathPrefix("config") {
    path("AuthSettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated = AppSettingsNeo4jWrapper.updateSettings(appSettings, "AUTH_SETTINGS")
          onComplete(maybeUpdated) {
            case Success(update) => update.status match {
              case true => complete(StatusCodes.OK, "Updated successfully")
              case false => complete(StatusCodes.OK, "Updated failed,,Retry!!")
            }
            case Failure(ex) =>
              ex match {
                case aie: IllegalArgumentException =>
                  logger.error("Update operation failed", ex)
                  complete(StatusCodes.OK, "Failed to update settings")
                case _ =>
                  logger.error("Update operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }

  } ~ pathPrefix("config") {
    path("ApplicationSettings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeDeleted = AppSettingsNeo4jWrapper.deleteSetting(appSettings, "GENERAL_SETTINGS")
          onComplete(maybeDeleted) {
            case Success(delete) => delete.status match {
              case true => complete(StatusCodes.OK, "Deleted successfully")
              case false => complete(StatusCodes.OK, "Deletion failed,,Retry!!")
            }
            case Failure(ex) =>
              ex match {
                case aie: IllegalArgumentException =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.OK, "Failed to delete settings")
                case _ =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }
  } ~ pathPrefix("config") {
    path("AuthSettings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeDelete = AppSettingsNeo4jWrapper.deleteSetting(appSettings, "AUTH_SETTINGS")
          onComplete(maybeDelete) {
            case Success(delete) => delete.status match {
              case true => complete(StatusCodes.OK, "Deleted successfully")
              case false => complete(StatusCodes.OK, "Deletion failed,,Retry!!")
            }
            case Failure(ex) =>
              ex match {
                case aie: IllegalArgumentException =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.OK, "Failed to delete settings")
                case _ =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }

  }

  val discoveryRoutes = pathPrefix("discover") {
    path("site") {
      put {
        entity(as[Site1]) { site =>
          val buildSite = Future {
            populateInstances(site)
          }
          onComplete(buildSite) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to save the Site with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to save the Site.")
          }
        }
      }
    } ~ path("site" / LongNumber) {
      siteId =>
        get {
          val siteObj = Future {
            Site1.fromNeo4jGraph(siteId)
          }
          onComplete(siteObj) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to get Site Object ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get Site")
          }
        }
    } ~ path("regions") {
      get {
        parameter("provider") {
          provider =>
            logger.info(s"Coming Provider $provider")
            val regions = Future {
              AGU.getRegions(InstanceProvider.toInstanceProvider(provider))
            }
            onComplete(regions) {
              case Success(response) => complete(StatusCodes.OK, response)
              case Failure(exception) =>
                logger.error(s"Unable to get regions ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to get Regions")
            }
        }
      }
    } ~ pathPrefix("site" / LongNumber) {
      siteId => pathPrefix("group" / LongNumber) {
        groupId =>
          logger.info(s"Site ID : $siteId , Group ID : $groupId")
          val instanceGroup = Future {
            InstanceGroup.fromNeo4jGraph(groupId)
          }
          onComplete(instanceGroup) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to get Instance Group ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get Instance Group")
          }
      }
    } ~ get {
      pathPrefix("site" / LongNumber) {
        siteId =>
          pathPrefix("groups" / Segment) {
            groupType =>
              val response = Future {
                val mayBeSite = Site1.fromNeo4jGraph(siteId)
                mayBeSite match {
                  case Some(site) => site.groupsList.filter(group => group.groupType.contains(groupType))
                  case None => throw new NotFoundException(s"Site Entity with ID : $siteId is Not Found")
                }
              }
              onComplete(response) {
                case Success(groups) => complete(StatusCodes.OK, groups)
                case Failure(exception) =>
                  logger.error(s"Unable to get Instance Groups ${exception.getMessage}", exception)
                  complete(StatusCodes.BadRequest, "Unable to get Instance Groups")
              }
          }
      }
    } ~ put {
      path("site" / LongNumber / "group") {
        siteId =>
          entity(as[InstanceGroup]) {
            instanceGroup =>
              val response = Future {
                val mayBeSite = Site1.fromNeo4jGraph(siteId)
                mayBeSite match {
                  case Some(site) => val groups = site.groupsList
                    val mayBeGroup = groups.find(group => instanceGroup.name.equals(group.name))
                    mayBeGroup match {
                      case Some(groupToSave) =>
                        val instanceGroupToSave = groupToSave.copy(instances = groupToSave.instances ::: instanceGroup.instances)
                        instanceGroupToSave.toNeo4jGraph(instanceGroupToSave)
                      case None =>
                        val instanceGroupNode = instanceGroup.toNeo4jGraph(instanceGroup)
                        val siteNode = Neo4jRepository.findNodeById(siteId)
                        Neo4jRepository.createRelation("HAS_InstanceGroup", siteNode.get, instanceGroupNode)
                    }
                  case None => throw new NotFoundException(s"Site Entity with ID : $siteId is Not Found")
                }
              }
              onComplete(response) {
                case Success(responseMessage) => complete(StatusCodes.OK, "Done")
                case Failure(exception) =>
                  logger.error(s"Unable to save the Instance group ${exception.getMessage}", exception)
                  complete(StatusCodes.BadRequest, "Unable save the instance group")
              }
          }
      }
    } ~ get {
      path("tags" / LongNumber) {
        siteId =>
          val tags = Future {
            val site = Site1.fromNeo4jGraph(siteId)
            if (site.nonEmpty) {
              site.get.instances.flatMap(instance => instance.tags.filter(tag => tag.key.equalsIgnoreCase(Constants.NAME_TAG_KEY)))
            } else {
              throw new NotFoundException(s"Site Entity with ID : $siteId is Not Found")
            }
          }
          onComplete(tags) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to get the Tags with Given Site ID : $siteId  ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get tags")
          }
      }
    }
  } ~ path("keypairs" / LongNumber) { siteId =>
    get {
      val listOfKeyPairs = Future {
        val mayBeSite = Site1.fromNeo4jGraph(siteId)
        mayBeSite match {
          case Some(site) =>
            val keyPairs = site.instances.flatMap { instance =>
              instance.sshAccessInfo.flatMap(x => Some(x.keyPair))
            }
            Page[KeyPairInfo](keyPairs)
          case None =>
            logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
            Page[KeyPairInfo](List.empty[KeyPairInfo])
        }
      }
      onComplete(listOfKeyPairs) {
        case Success(successResponse) => complete(StatusCodes.OK, successResponse)
        case Failure(ex) =>
          logger.error(s"Unable to get List; Failed with ${ex.getMessage}", ex)
          complete(StatusCodes.BadRequest, "Unable to get List of KeyPairs")
      }
    }
  } ~ path("site" / LongNumber) { siteId =>
    get {
      parameter("type") { view =>
        val filteredSite = Future {
          val mayBeSite = Site1.fromNeo4jGraph(siteId)
          mayBeSite match {
            case Some(site) =>
              val viewType = ViewType.toViewType(view)
              val listOfFilteredInstances = site.instances.map { instance =>
                viewType match {
                  case OPERATIONS => filterInstanceViewOperations(instance, ViewLevel.toViewLevel("SUMMARY"))
                  case ARCHITECTURE => filterInstanceViewArchitecture(instance, ViewLevel.toViewLevel("SUMMARY"))
                  case LIST => filterInstanceViewList(instance, ViewLevel.toViewLevel("SUMMARY"))
                }

              }
              Some(Site1(site.id, site.siteName, listOfFilteredInstances, site.reservedInstanceDetails,
                site.filters, site.loadBalancers, site.scalingGroups, site.groupsList))
            case None =>
              logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
              None
          }
        }
        onComplete(filteredSite) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(ex) =>
            logger.error(s"Unable to get Filtered Site; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, "Unable to get Filtered Site")
        }
      }
    }
  } ~ path("site" / "filter" / LongNumber) { siteId =>
    put {
      entity(as[SiteFilter]) { siteFilter =>
        val filteredSite = Future {
          val filters = siteFilter.filters
          populateFilteredInstances(siteId, filters)
        }
        onComplete(filteredSite) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(ex) =>
            logger.error(s"Unable to populate Filtered Instances; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, "Unable to populated Filtered Instances")
        }
      }
    }
  }


  def siteServiceRoutes = pathPrefix("sites") {
    path("site" / LongNumber) { siteId =>
      get {
        parameter("type") { view =>
          val filteredSite = Future {
            val siteNode = Site1.fromNeo4jGraph(siteId)
            siteNode match {
              case Some(site) =>
                val viewType = ViewType.toViewType(view)
                val filteredInstances = site.instances.map { instance =>
                  viewType match {
                    case OPERATIONS => filterInstanceViewOperations(instance, ViewLevel.toViewLevel("SUMMARY"))
                    case ARCHITECTURE => filterInstanceViewArchitecture(instance, ViewLevel.toViewLevel("SUMMARY"))
                    case LIST => filterInstanceViewList(instance, ViewLevel.toViewLevel("SUMMARY"))
                  }
                }
                Some(Site1(site.id, site.siteName, filteredInstances, site.reservedInstanceDetails, site.filters, site.loadBalancers, site.scalingGroups, site.groupsList))
              case None =>
                logger.warn(s"Failed in fromNeo4jGraph of Site for siteId : $siteId")
                None
            }
          }
          onComplete(filteredSite) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to retrieve Filtered Sites. Failed with exception: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to retrieve Filtered Sites")
          }
        }
      }
    } ~ path(LongNumber / "instances") { siteId =>
      get {
        parameter("type") { view =>
          val instancesList = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) =>
                val instances = site.instances
                Page[Instance](instances)
              case None =>
                logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
                Page[Instance](List.empty[Instance])
            }
          }
          onComplete(instancesList) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get List; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to get List of Instances")
          }
        }
      }
    } ~ path(LongNumber / "lbs") { siteId =>
      get {
        parameter("type") { view =>
          val loadBalancersList = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) =>
                val loadBalancers = site.loadBalancers
                Page[LoadBalancer](loadBalancers)
              case None =>
                logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
                Page[LoadBalancer](List.empty[LoadBalancer])
            }
          }
          onComplete(loadBalancersList) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get List; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to get List of Load Balancers")
          }
        }
      }
    } ~ path(LongNumber / "scalegroups") { siteId =>
      get {
        parameter("type") {
          viewType =>
            val scalingGroups = Future {
              val mayBeSite = Site1.fromNeo4jGraph(siteId)
              mayBeSite.map(site => site.scalingGroups).getOrElse(List.empty[ScalingGroup])
            }

            onComplete(scalingGroups) {
              case Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case Failure(exception) =>
                logger.error(s"Unable to get ScalingGroups. Failed with : ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to get Scaling Groups.")
            }
        }
      }
    } ~ path(LongNumber / "instances") { siteId =>
      put {
        entity(as[Instance]) { instance =>
          val buildInstance = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) =>
                val listOfInstances = site.instances
                val newListOfInstances = instance :: listOfInstances
                val siteToSave = Site1(site.id, site.siteName, newListOfInstances, site.reservedInstanceDetails, site.filters, site.loadBalancers, site.scalingGroups, site.groupsList)
                siteToSave.toNeo4jGraph(siteToSave)
                "Instance added successfully to Site"
              case None =>
                throw new NotFoundException(s"Site Entity with ID : $siteId Not Found")
            }
          }

          onComplete(buildInstance) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to build Instance. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to build Instance.")
          }
        }
      }
    } ~ path(LongNumber / "instances" / Segment) { (siteId, id) =>
      get {
        parameter("type") {
          view =>
            val instance = Future {
              val mayBeSite = Site1.fromNeo4jGraph(siteId)
              val mayBeInstance = mayBeSite match {
                case Some(site) => site.instances.find(instance => instance.instanceId.contains(id))
                case None => throw new NotFoundException(s"Site Entity with ID : $siteId Not Found")
              }
              mayBeInstance.map { instance =>
                val viewType = ViewType.toViewType(view)
                val filteredInstance = viewType match {
                  case OPERATIONS => filterInstanceViewOperations(instance, ViewLevel.toViewLevel("DETAILED"))
                  case ARCHITECTURE => filterInstanceViewArchitecture(instance, ViewLevel.toViewLevel("DETAILED"))
                  case LIST => filterInstanceViewList(instance, ViewLevel.toViewLevel("DETAILED"))
                }
                filteredInstance
              }
            }
            onComplete(instance) {
              case Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case Failure(exception) =>
                logger.error(s"Unable to get Instance. Failed with : ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to get Instance.")
            }
        }
      }
    } ~ path(LongNumber / "delta") { siteId =>
      get {
        val siteDelta = Future {
          val mayBeSite = Site1.fromNeo4jGraph(siteId)
          mayBeSite match {
            case Some(site) =>
              val instancesBeforeSync = site.instances
              val synchedSite = populateInstances(site)
              val filteredSite = populateFilteredInstances(siteId, List.empty[Filter])
              saveCachedSite(siteId)
              filteredSite match {
                case Some(siteInCache) =>
                  val instancesAfterSync = siteInCache.instances
                  val beforeSyncInstanceIds = instancesBeforeSync.flatMap (instance => instance.instanceId).toSet
                  val afterSyncInstanceIds = instancesAfterSync.flatMap (instance => instance.instanceId).toSet
                  val commonInstanceIds = beforeSyncInstanceIds.intersect (afterSyncInstanceIds)
                  val deletedInstances = instancesBeforeSync.filterNot {
                    instance =>
                      instance.instanceId.exists { id =>
                        commonInstanceIds.contains(id)
                      }
                  }
                  val addedInstances = instancesAfterSync.filterNot {
                    instance =>
                      instance.instanceId.exists { id =>
                        commonInstanceIds.contains(id)
                      }
                  }
                  val deltaStatus = if (deletedInstances.nonEmpty || addedInstances.nonEmpty) {
                    SiteDeltaStatus.toDeltaStatus ("CHANGED")
                  } else {
                    SiteDeltaStatus.toDeltaStatus ("UNCHANGED")
                  }
                  Some(SiteDelta (site.id, deltaStatus, addedInstances, deletedInstances))
                case None =>
                  logger.warn(s"could not get site from cache for siteId : $siteId")
                  None
              }
            case None =>
              logger.warn(s"could not get site with siteId : $siteId")
              None
          }
        }
        onComplete(siteDelta) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to get Site Delta. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Site Delta.")
        }
      }
    }
  }

  def siteServices : Route = pathPrefix("site") {
    get {
      parameters('viewLevel.as[String]) {
        (viewLevel) =>
          val result = Future {
            logger.info("View level is..." + viewLevel)
            //Instead of applying map and flat operations at distinct  places,flatMap used directly though container holds only List[Nodes]
            //Use of map here produces results like List[Option[Site]] but List[Site1] would be better option for next operations.
            Neo4jRepository.getNodesByLabel("Site1").flatMap{ siteNode =>
              Site1.fromNeo4jGraph(siteNode.getId).map {
                siteObj => SiteViewFilter.filterInstance(siteObj, ViewLevel.toViewLevel(viewLevel))

              }
            }
          }
          onComplete(result) {
            case Success(sitesList) => complete(StatusCodes.OK, sitesList)
            case Failure(ex) => logger.error("Unable to retrieve sites information", ex)
              complete(StatusCodes.BadRequest, "Failed to get results")
          }
      }
    }
  } ~ path("site" / LongNumber) {
    siteId => delete {
      val maybeDelete = Future {
        Site1.delete(siteId)
      }
      onComplete(maybeDelete) {
        case Success(executionStatus) => complete(StatusCodes.OK, executionStatus.msg)
        case Failure(ex) => logger.info("Failed to delete entity", ex)
          complete(StatusCodes.BadRequest, "Deletion failed")
      }
    }
  } ~ path("site" / LongNumber/"instances"/ Segment) {
    (siteId,instanceId) =>  {
      delete {
        val mayBeDelete = Future {
          SiteManagerImpl.deleteIntanceFromSite(siteId, instanceId)
        }
        onComplete(mayBeDelete) {
          case Success(executionStatus) => complete(StatusCodes.OK, executionStatus.msg)
          case Failure(ex) => logger.error("Failed to delete the insatnce", ex)
            complete(StatusCodes.BadRequest, "Failed to delete instance")
        }
      }
    }
  } ~
    path("site"/LongNumber/"policies"/Segment){
      (siteId,policyId) => {
        delete {
          val maybeDelete = Future {
            SiteManagerImpl.deletePolicy(policyId)
          }
          onComplete(maybeDelete){
            case Success(executionStatus) => complete(StatusCodes.OK,executionStatus.msg)
            case Failure(ex) => logger.info(s"Error while deleting policy $policyId",ex)
              complete(StatusCodes.BadRequest,"Error while deleting policy")
          }
        }
      }
    }


  val route: Route = siteServices ~ userRoute ~ keyPairRoute ~ catalogRoutes ~ appSettingServiceRoutes ~
    apmServiceRoutes ~ nodeRoutes ~ appsettingRoutes ~ discoveryRoutes ~ siteServiceRoutes


  val bindingFuture = Http().bindAndHandle(route, AGU.HOST, AGU.PORT)
  logger.info(s"Server online at http://${AGU.HOST}:${AGU.PORT}")


  def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    User.fromNeo4jGraph(userId) match {
      case Some(user) => user.publicKeys.dropWhile(_.id.get != keyId).headOption
      case None => None
    }
  }

  def getOrCreateKeyPair(keyName: String, keyMaterial: String, keyFilePath: Option[String], status: KeyPairStatus, defaultUser: Option[String], passPhase: Option[String]): KeyPairInfo = {
    val mayBeKeyPair = Neo4jRepository.getSingleNodeByLabelAndProperty("KeyPairInfo", "keyName", keyName).flatMap(node => KeyPairInfo.fromNeo4jGraph(node.getId))

    mayBeKeyPair match {
      case Some(keyPairInfo) =>
        KeyPairInfo(keyPairInfo.id, keyName, keyPairInfo.keyFingerprint, keyMaterial, if (keyFilePath.isEmpty) keyPairInfo.filePath else keyFilePath, status, if (defaultUser.isEmpty) keyPairInfo.defaultUser else defaultUser, if (passPhase.isEmpty) keyPairInfo.passPhrase else passPhase)
      case None => KeyPairInfo(keyName, keyMaterial, keyFilePath, status)
    }
  }

  def saveKeyPair(keyPairInfo: KeyPairInfo): Option[KeyPairInfo] = {
    val filePath = getKeyFilePath(keyPairInfo.keyName)
    try {
      FileUtils.createDirectories(keyFilesDir)
      FileUtils.saveContentToFile(filePath, keyPairInfo.keyMaterial)
      // TODO: change permissions to 600
    } catch {
      case e: Throwable => logger.error(e.getMessage, e)
    }
    val node = keyPairInfo.toNeo4jGraph(KeyPairInfo(keyPairInfo.id, keyPairInfo.keyName, keyPairInfo.keyFingerprint, keyPairInfo.keyMaterial, Some(filePath), keyPairInfo.status, keyPairInfo.defaultUser, keyPairInfo.passPhrase))
    KeyPairInfo.fromNeo4jGraph(node.getId)
  }

  val keyFilesDir: String = s"${Constants.tempDirectoryLocation}${Constants.FILE_SEPARATOR}"

  def getKeyFilePath(keyName: String): String = s"$keyFilesDir$keyName.pem"

  def getAPMServers: mutable.MutableList[APMServerDetails] = {
    logger.debug(s"Executing $getClass :: getAPMServers")
    val nodes = APMServerDetails.getAllEntities
    logger.debug(s"Getting all entities and size is :${nodes.size}")
    val list = mutable.MutableList.empty[APMServerDetails]
    nodes.foreach {
      node =>
        val aPMServerDetails = APMServerDetails.fromNeo4jGraph(node.getId)
        aPMServerDetails match {
          case Some(serverDetails) => list.+=(serverDetails)
          case _ => logger.warn(s"Node not found with ID: ${node.getId}")
        }
    }
    logger.debug(s"Reurning list of APM Servers $list")
    list
  }

  def filterInstanceViewList(instance: Instance, viewLevel: ViewLevel): Instance = {
    viewLevel match {
      case SUMMARY =>
        Instance(instance.id, instance.instanceId, instance.name, instance.state, instance.instanceType, None, None, instance.publicDnsName,
          None, None, None, instance.tags, instance.sshAccessInfo, List.empty[InstanceConnection], List.empty[InstanceConnection],
          Set.empty[ProcessInfo], None, List.empty[InstanceUser], instance.account, instance.availabilityZone, None, instance.privateIpAddress,
          instance.publicIpAddress, None, None, None, List.empty[InstanceBlockDeviceMappingInfo], List.empty[SecurityGroupInfo],
          instance.reservedInstance, instance.region)
      case DETAILED =>
        Instance(instance.id, instance.instanceId, instance.name, instance.state, instance.instanceType, None, None, instance.publicDnsName,
          None, instance.memoryInfo, instance.rootDiskInfo, instance.tags, None, List.empty[InstanceConnection], List.empty[InstanceConnection],
          Set.empty[ProcessInfo], instance.image, List.empty[InstanceUser], instance.account, instance.availabilityZone, instance.privateDnsName, instance.privateIpAddress,
          None, instance.elasticIP, instance.monitoring, instance.rootDeviceType, List.empty[InstanceBlockDeviceMappingInfo], instance.securityGroups,
          instance.reservedInstance, instance.region)
    }
  }

  def filterInstanceViewArchitecture(instance: Instance, viewLevel: ViewLevel): Instance = {
    viewLevel match {
      case SUMMARY =>
        Instance(instance.id, instance.instanceId, instance.name, instance.state, instance.instanceType, None, None, instance.publicDnsName,
          None, None, None, instance.tags, instance.sshAccessInfo, instance.liveConnections, List.empty[InstanceConnection],
          Set.empty[ProcessInfo], None, List.empty[InstanceUser], instance.account, instance.availabilityZone, None, instance.privateIpAddress,
          instance.publicIpAddress, None, None, None, List.empty[InstanceBlockDeviceMappingInfo], List.empty[SecurityGroupInfo],
          instance.reservedInstance, instance.region)
      case DETAILED =>
        Instance(instance.id, instance.instanceId, instance.name, instance.state, instance.instanceType, None, None, instance.publicDnsName,
          None, instance.memoryInfo, instance.rootDiskInfo, instance.tags, None, List.empty[InstanceConnection], List.empty[InstanceConnection],
          instance.processes, instance.image, List.empty[InstanceUser], instance.account, instance.availabilityZone, instance.privateDnsName, instance.privateIpAddress,
          None, instance.elasticIP, instance.monitoring, instance.rootDeviceType, List.empty[InstanceBlockDeviceMappingInfo], instance.securityGroups,
          instance.reservedInstance, instance.region)
    }
  }

  def filterInstanceViewOperations(instance: Instance, viewLevel: ViewLevel): Instance = {
    viewLevel match {
      case SUMMARY =>
        Instance(instance.id, instance.instanceId, instance.name, instance.state, instance.instanceType, None, None, instance.publicDnsName,
          None, None, None, instance.tags, instance.sshAccessInfo, instance.liveConnections, instance.estimatedConnections,
          Set.empty[ProcessInfo], None, List.empty[InstanceUser], instance.account, instance.availabilityZone, None, instance.privateIpAddress,
          instance.publicIpAddress, None, None, None, List.empty[InstanceBlockDeviceMappingInfo], List.empty[SecurityGroupInfo],
          instance.reservedInstance, instance.region)
      case DETAILED =>
        Instance(instance.id, instance.instanceId, instance.name, instance.state, instance.instanceType, None, None, instance.publicDnsName,
          None, instance.memoryInfo, instance.rootDiskInfo, instance.tags, None, List.empty[InstanceConnection], List.empty[InstanceConnection],
          instance.processes, instance.image, List.empty[InstanceUser], instance.account, instance.availabilityZone, instance.privateDnsName, instance.privateIpAddress,
          None, instance.elasticIP, instance.monitoring, instance.rootDeviceType, instance.blockDeviceMappings, instance.securityGroups,
          instance.reservedInstance, instance.region)
    }
  }


  def getInstancesToSave(filteredInstances: Set[Instance]): List[Instance] = {

    val instanceIds = filteredInstances.map(instance => instance.instanceId)
    val existingInstances = instanceIds.flatMap { id =>
      val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("Instance", "instanceId", id)
      mayBeNode.flatMap(node => Instance.fromNeo4jGraph(node.getId))
    }.toList

    val existingInstancesMap = existingInstances.map(instance => instance.instanceId -> instance).toMap
    val instancesToSave = filteredInstances.map { instance =>
      val existingInstance = existingInstancesMap.get(instance.instanceId)
      existingInstance match {
        case Some(e) =>
          Instance(e.id, e.instanceId, e.name, instance.state, e.instanceType, e.platform, e.architecture, instance.publicDnsName,
            e.launchTime, e.memoryInfo, e.rootDiskInfo, e.tags, e.sshAccessInfo, e.liveConnections, e.estimatedConnections, e.processes,
            e.image, e.existingUsers, e.account, e.availabilityZone, instance.privateDnsName, instance.privateIpAddress,
            instance.publicIpAddress, e.elasticIP, e.monitoring, e.rootDeviceType, e.blockDeviceMappings, e.securityGroups,
            instance.reservedInstance, e.region)

        case None => instance
      }
    }.toList
    instancesToSave
  }

  def getLoadBalancersToSave(lbs: List[LoadBalancer]): List[LoadBalancer] = {
    val lbNames = lbs.map(lb => lb.name)
    val existingLbs = lbNames.flatMap { name =>
      val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("LoadBalancer", "name", name)
      mayBeNode.flatMap(node => LoadBalancer.fromNeo4jGraph(node.getId))
    }
    val existingLbsMap = existingLbs.map(lb => lb.name -> lb).toMap
    val lbsToSave = lbs.map { lb =>
      val existingLb = existingLbsMap.get(lb.name)
      existingLb match {
        case Some(l) =>
          LoadBalancer(l.id, lb.name, l.vpcId, l.region, lb.instanceIds, l.availabilityZones)
        case None => lb
      }
    }
    lbsToSave
  }

  def getScalingGroupsToSave(sgs: List[ScalingGroup]): List[ScalingGroup] = {
    val sgNames = sgs.map(sg => sg.name)
    val existingSgs = sgNames.flatMap { name =>
      val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("ScalingGroup", "name", name)
      mayBeNode.flatMap(node => ScalingGroup.fromNeo4jGraph(node.getId))
    }
    val existingSgsMap = existingSgs.map(sg => sg.name -> sg).toMap
    val sgsToSave = sgs.map { sg =>
      val existingSg = existingSgsMap.get(sg.name)
      existingSg match {
        case Some(s) =>
          ScalingGroup(s.id, sg.name, sg.launchConfigurationName, sg.status, s.availabilityZones, sg.instanceIds,
            s.loadBalancerNames, s.tags, s.desiredCapacity, s.maxCapacity, s.minCapacity)
        case None => sg
      }
    }
    sgsToSave
  }

  def getReservedInstancesToSave(rInstances: List[ReservedInstanceDetails]): List[ReservedInstanceDetails] = {
    val rIds = rInstances.map(rInstance => rInstance.reservedInstancesId)
    val existingRInstances = rIds.flatMap { id =>
      val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("ReservedInstanceDetails", "reservedInstancesId", id)
      mayBeNode.flatMap(node => ReservedInstanceDetails.fromNeo4jGraph(node.getId))
    }
    val existingRInstancesMap = existingRInstances.map(rInstances => rInstances.reservedInstancesId -> rInstances).toMap
    val rInstancesToSave = rInstances.map { rInstance =>
      val existingRInstance = existingRInstancesMap.get(rInstance.reservedInstancesId)
      existingRInstance match {
        case Some(r) =>
          ReservedInstanceDetails(r.id, r.reservedInstancesId, rInstance.instanceType, rInstance.availabilityZone, rInstance.tenancy,
            rInstance.offeringType, rInstance.productDescription, rInstance.count)
        case None => rInstance
      }
    }
    rInstancesToSave
  }

  def filterExistsInTags(instance: Instance, filter: Filter): Boolean = {
    val filterType = filter.filterType
    val filterValues = filter.values
    filterType match {
      case TAGS =>
        val tags = instance.tags
        val tagValues = tags.map(tag => tag.value)
        filterValues.intersect(tagValues).nonEmpty
      case STATUS =>
        instance.state match {
          case Some(state) => filterValues.contains(state)
          case None => false
        }
      case _ => false
    }
  }

  def populateFilteredInstances(siteId: Long,  filters: List[Filter]) : Option[Site1] = {
    val mayBeSite = cachedSite.get(siteId)
    mayBeSite match {
      case Some (site) =>
        val instances = site.instances
        val (siteFiltersToSave, filteredInstances) = if (filters.nonEmpty) {
          val siteFilters = site.filters
          val listOfSiteFilters = siteFilters.map {
            siteFilter => SiteFilter (siteFilter.id, siteFilter.accountInfo, filters)
          }
          val listOfInstances = instances.flatMap {
            instance =>
              val filterExists = filters.find (filter => filterExistsInTags (instance, filter) )
              filterExists.map (filter => instance)
          }
          (listOfSiteFilters, listOfInstances)
        }
        else {
          (site.filters, instances)
        }
        val instancesToSave = getInstancesToSave (filteredInstances.toSet)
        val lbsToSave = getLoadBalancersToSave (site.loadBalancers)
        val sgsToSave = getScalingGroupsToSave (site.scalingGroups)
        val rInstancesToSave = getReservedInstancesToSave (site.reservedInstanceDetails)
        val siteToSave = Site1 (site.id, site.siteName, instancesToSave, rInstancesToSave, siteFiltersToSave, lbsToSave, sgsToSave, site.groupsList)
        siteToSave.toNeo4jGraph (siteToSave)
        cachedSite.put (siteId, siteToSave)
        Some (siteToSave)
      case None => logger.warn (s"could not get Site from cache for siteId : $siteId")
        None
    }
  }

  def populateInstances(site: Site1): Site1 = {
    val siteFilters = site.filters
    logger.info(s"Parsing instance : ${site.instances}")
    val computedResult = siteFilters.foldLeft((List[Instance](), List[ReservedInstanceDetails](), List.empty[LoadBalancer], List.empty[ScalingGroup])) {
      (result, siteFilter) =>
        val accountInfo = siteFilter.accountInfo
        val amazonEC2 = AWSComputeAPI.getComputeAPI(accountInfo)
        val instances = AWSComputeAPI.getInstances(amazonEC2, accountInfo)
        val reservedInstanceDetails = AWSComputeAPI.getReservedInstances(amazonEC2)
        val loadBalancers = AWSComputeAPI.getLoadBalancers(accountInfo)
        val scalingGroup = AWSComputeAPI.getAutoScalingGroups(accountInfo)
        (result._1 ::: instances, result._2 ::: reservedInstanceDetails, result._3 ::: loadBalancers, result._4 ::: scalingGroup)
    }
    val site1 = Site1(None, site.siteName, computedResult._1, computedResult._2, site.filters, computedResult._3, computedResult._4, List())
    site1.toNeo4jGraph(site1)
    site1
  }

  def saveCachedSite(siteId: Long): Boolean = {
    val mayBeSite = cachedSite.get(siteId)
    mayBeSite.foreach { site =>
      site.toNeo4jGraph(site)
      cachedSite.remove(siteId)
    }
    mayBeSite.isDefined
  }

}