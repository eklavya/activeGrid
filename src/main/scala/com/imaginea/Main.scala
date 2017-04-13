package com.imaginea

import java.io.{File, FileInputStream, FileOutputStream, IOException, PrintWriter}
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}
import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.beust.jcommander.JCommander
import com.imaginea.activegrid.core.models.{InstanceGroup, KeyPairInfo, _}
import com.imaginea.activegrid.core.utils.{Constants, FileUtils, ActiveGridUtils => AGU}
import com.imaginea.actors.WorkflowDDHandler
import com.jcraft.jsch.{ChannelExec, JSch, JSchException}
import com.typesafe.scalalogging.Logger
import org.apache.commons.io.{FilenameUtils, FileUtils => CommonFileUtils}
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.mutable
import scala.concurrent.duration.{Duration, DurationLong}
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Random, Success}

object Main extends App {

  implicit val system = ActorSystem("ClusterSystem")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(15.seconds)
  val ansibleWorkflowProcessor = AnsibleWorkflowProcessor
  val currentWorkflows = mutable.HashMap.empty[Long, WorkflowContext]
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {
    override def write(obj: KeyPairStatus): JsValue = JsString(obj.name.toString)

    override def read(json: JsValue): KeyPairStatus = json match {
      case JsString(str) => KeyPairStatus.toKeyPairStatus(str)
      case _ => throw DeserializationException("Enum string expected")
    }
  }

  implicit val keyPairInfoFormat = jsonFormat(KeyPairInfo.apply, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser",
    "passPhrase")
  implicit val pageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")
  implicit val userFormat = jsonFormat(User.apply, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked",
    "credentialsNonExpired", "enabled", "displayName")
  implicit val pageUsersFomat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")
  implicit val sshKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")
  implicit val softwareFormat = jsonFormat(Software.apply, "id", "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
  implicit val softwarePageFormat = jsonFormat4(Page[Software])
  implicit val imageFormat = jsonFormat(ImageInfo.apply, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform",
    "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
  implicit val pageImageFormat = jsonFormat4(Page[ImageInfo])
  implicit val appSettingsFormat = jsonFormat(AppSettings.apply, "id", "settings", "authSettings")
  implicit val portRangeFormat = jsonFormat(PortRange.apply, "id", "fromPort", "toPort")
  implicit val sshAccessInfoFormat = jsonFormat(SSHAccessInfo.apply, "id", "keyPair", "userName", "port")
  implicit val instanceConnectionFormat = jsonFormat(InstanceConnection.apply, "id", "sourceNodeId", "targetNodeId", "portRanges")
  implicit val processInfoFormat = jsonFormat(ProcessInfo.apply, "id", "pid", "parentPid", "name", "command", "owner", "residentBytes", "software",
    "softwareVersion")
  implicit val instanceUserFormat = jsonFormat(InstanceUser.apply, "id", "userName", "publicKeys")
  implicit val instanceFlavorFormat = jsonFormat(InstanceFlavor.apply, "name", "cpuCount", "memory", "rootDisk")
  implicit val pageInstFormat = jsonFormat4(Page[InstanceFlavor])
  implicit val storageInfoFormat = jsonFormat(StorageInfo.apply, "id", "used", "total")
  implicit val keyValueInfoFormat = jsonFormat(KeyValueInfo.apply, "id", "key", "value")

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

  implicit val ipPermissionInfoFormat = jsonFormat(IpPermissionInfo.apply, "id", "fromPort", "toPort", "ipProtocol", "groupIds", "ipRanges")

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

  implicit val accountInfoFormat = jsonFormat(AccountInfo.apply, "id", "accountId", "providerType", "ownerAlias", "accessKey", "secretKey", "regionName",
    "regions", "networkCIDR")
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
      val fields = longToJsField(fieldNames.head, i.id) ++
        stringToJsField(fieldNames(1), i.instanceId) ++
        List((fieldNames(2), JsString(i.name))) ++
        stringToJsField(fieldNames(3), i.state) ++
        stringToJsField(fieldNames(4), i.instanceType) ++
        stringToJsField(fieldNames(5), i.platform) ++
        stringToJsField(fieldNames(6), i.architecture) ++
        stringToJsField(fieldNames(7), i.publicDnsName) ++
        longToJsField(fieldNames(8), i.launchTime) ++
        objectToJsValue[StorageInfo](fieldNames(9), i.memoryInfo, storageInfoFormat) ++
        objectToJsValue[StorageInfo](fieldNames(10), i.rootDiskInfo, storageInfoFormat) ++
        listToJsValue[KeyValueInfo](fieldNames(11), i.tags, keyValueInfoFormat) ++
        objectToJsValue[SSHAccessInfo](fieldNames(12), i.sshAccessInfo, sshAccessInfoFormat) ++
        listToJsValue[InstanceConnection](fieldNames(13), i.liveConnections, instanceConnectionFormat) ++
        listToJsValue[InstanceConnection](fieldNames(14), i.estimatedConnections, instanceConnectionFormat) ++
        setToJsValue[ProcessInfo](fieldNames(15), i.processes, processInfoFormat) ++
        objectToJsValue[ImageInfo](fieldNames(16), i.image, imageFormat) ++
        listToJsValue[InstanceUser](fieldNames(17), i.existingUsers, instanceUserFormat) ++
        objectToJsValue[AccountInfo](fieldNames(18), i.account, accountInfoFormat) ++
        stringToJsField(fieldNames(19), i.availabilityZone) ++
        stringToJsField(fieldNames(20), i.privateDnsName) ++
        stringToJsField(fieldNames(21), i.privateIpAddress) ++
        stringToJsField(fieldNames(22), i.publicIpAddress) ++
        stringToJsField(fieldNames(23), i.elasticIP) ++
        stringToJsField(fieldNames(24), i.monitoring) ++
        stringToJsField(fieldNames(25), i.rootDeviceType) ++
        listToJsValue[InstanceBlockDeviceMappingInfo](fieldNames(26), i.blockDeviceMappings, instanceBlockingFormat) ++
        listToJsValue[SecurityGroupInfo](fieldNames(27), i.securityGroups, securityGroupsFormat) ++
        List((fieldNames(28), JsBoolean(i.reservedInstance))) ++
        stringToJsField(fieldNames(29), i.region)
      // scalastyle:on magic.number
      JsObject(fields: _*)
    }

    def stringToJsField(fieldName: String, fieldValue: Option[String], rest: List[JsField] = Nil): List[(String, JsValue)] = {
      fieldValue match {
        case Some(x) => (fieldName, JsString(x)) :: rest
        case None => rest
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
  }

  implicit val PageInstanceFormat = jsonFormat4(Page[Instance])
  implicit val siteFormat = jsonFormat(Site.apply, "id", "instances", "siteName", "groupBy")
  implicit val appSettings = jsonFormat(ApplicationSettings.apply, "id", "settings", "authSettings")
  implicit val resourceACLFormat = jsonFormat(ResourceACL.apply, "id", "resources", "permission", "resourceIds")
  implicit val userGroupFormat = jsonFormat(UserGroup.apply, "id", "name", "users", "accesses")
  implicit val pageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")
  implicit val reservedInstanceDetailsFormat = jsonFormat(ReservedInstanceDetails.apply, "id", "instanceType", "reservedInstancesId", "availabilityZone",
    "tenancy", "offeringType", "productDescription", "count")
  implicit val siteACLFormat = jsonFormat(SiteACL.apply, "id", "name", "site", "instances", "groups")
  implicit val pageSiteACLFormat = jsonFormat(Page[SiteACL], "startIndex", "count", "totalObjects", "objects")
  implicit val instanceGroupFormat = jsonFormat(InstanceGroup.apply, "id", "groupType", "name", "instances")

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

  implicit val filterFormat = jsonFormat(Filter.apply, "id", "filterType", "values")
  implicit val siteFilterFormat = jsonFormat(SiteFilter.apply, "id", "accountInfo", "filters")
  implicit val loadBalancerFormat = jsonFormat(LoadBalancer.apply, "id", "name", "vpcId", "region", "instanceIds", "availabilityZones")
  implicit val pageLoadBalancerFormat = jsonFormat4(Page[LoadBalancer])
  implicit val scalingGroupFormat = jsonFormat(ScalingGroup.apply, "id", "name", "launchConfigurationName", "status", "availabilityZones", "instanceIds",
    "loadBalancerNames", "tags", "desiredCapacity", "maxCapacity", "minCapacity")

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


  implicit object SiteFormat extends RootJsonFormat[Site1] {
    override def write(i: Site1): JsValue = {
      val fieldNames = List("id", "siteName", "instances", "reservedInstanceDetails", "filters",
        "loadBalancers", "scalingGroups", "groupsList", "applications", "groupBy")
      // scalastyle:off magic.number
      val fields = AGU.longToJsField(fieldNames(0), i.id) ++
        AGU.stringToJsField(fieldNames(1), Some(i.siteName)) ++
        AGU.listToJsValue[Instance]("instances", i.instances, instanceFormat) ++
        AGU.listToJsValue[ReservedInstanceDetails]("reservedInstanceDetails", i.reservedInstanceDetails, reservedInstanceDetailsFormat) ++
        AGU.listToJsValue[SiteFilter]("filters", i.filters, siteFilterFormat) ++
        AGU.listToJsValue[LoadBalancer]("loadBalancers", i.loadBalancers, loadBalancerFormat) ++
        AGU.listToJsValue[ScalingGroup]("scalingGroups", i.scalingGroups, scalingGroupFormat) ++
        AGU.listToJsValue[InstanceGroup]("groupsList", i.groupsList, instanceGroupFormat)
      // scalastyle:on magic.number
      JsObject(fields: _*)
    }


    override def read(json: JsValue): Site1 = {
      json match {
        case JsObject(map) =>
          Site1(
            AGU.getProperty[Long](map, "id"),
            AGU.getProperty[String](map, "siteName").getOrElse(""),
            AGU.getObjectsFromJson[Instance](map, "instances", instanceFormat),
            AGU.getObjectsFromJson[ReservedInstanceDetails](map, "reservedInstanceDetails", reservedInstanceDetailsFormat),
            AGU.getObjectsFromJson[SiteFilter](map, "filters", siteFilterFormat),
            AGU.getObjectsFromJson[LoadBalancer](map, "loadBalancer", loadBalancerFormat),
            AGU.getObjectsFromJson[ScalingGroup](map, "scalingGroups", scalingGroupFormat),
            AGU.getObjectsFromJson[InstanceGroup](map, "instanceGroups", instanceGroupFormat),
            List.empty[Application],
            AGU.getProperty[String](map, "groupBy").getOrElse(""),
            AGU.getObjectsFromJson[AutoScalingPolicy](map, "scalingPolicies", autoScalingPolicyJson)
          )
        case _ => logger.debug("Not a JsObject")
          Site1(0L)
      }
    }

  }

  implicit val apmServerDetailsFormat = jsonFormat(APMServerDetails.apply, "id", "name", "serverUrl", "monitoredSite", "provider", "headers")

  implicit val applicationTierFormat = jsonFormat5(ApplicationTier.apply)
  implicit val applicationFormat = jsonFormat9(Application.apply)

  implicit val metricTypeFormat = MetricTypeFormat
  implicit val unitTypeJson = UnitTypeJson
  implicit val conditionTypeJson = ConditionTypeJson
  implicit val scaleTypeJson = ScaleTypeJson
  implicit val policyConditionJson = jsonFormat8(PolicyCondition.apply)

  implicit object policyTypeFormat extends RootJsonFormat[PolicyType] {
    override def write(obj: PolicyType): JsValue = obj.plcyType.asInstanceOf[JsValue]

    override def read(json: JsValue): PolicyType = {
      json match {
        case JsString(str) => PolicyType.toType(str)
        case _ => throw DeserializationException("Unable to deserialize PolicyType")
      }
    }
  }

  implicit val autoScalingPolicyJson = jsonFormat8(AutoScalingPolicy.apply)
  implicit val site1Format = jsonFormat(Site1.apply, "id", "siteName", "instances", "reservedInstanceDetails", "filters", "loadBalancers", "scalingGroups",
    "groupsList", "applications", "groupBy", "scalingPolicies")
  implicit val pageApplicationFormat = jsonFormat4(Page[Application])

  implicit object esQueryTypeFormat extends RootJsonFormat[EsQueryType] {
    override def write(obj: EsQueryType): JsValue = obj.queryType.asInstanceOf[JsValue]

    override def read(json: JsValue): EsQueryType = {
      json match {
        case JsString(str) => EsQueryType.convertToQueryType(str)
        case _ => throw DeserializationException("Unable to deserialize QueryType")
      }
    }
  }

  implicit val esQueryFieldFormat = jsonFormat2(EsQueryField.apply)
  implicit val esSearchQueryFormat = jsonFormat6(EsSearchQuery.apply)
  implicit val esSearchResponseFormat = jsonFormat3(EsSearchResponse.apply)
  implicit val dataPointFormat = jsonFormat(DataPoint.apply, "timestamp", "value")
  implicit val resouceUtilizationFormat = jsonFormat(ResourceUtilization.apply, "target", "dataPoints")

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
  implicit val pluginFormat = jsonFormat3(PlugIn.apply)

  implicit object WorkflowModeFormat extends RootJsonFormat[WorkflowMode] {
    override def write(obj: WorkflowMode): JsValue = {
      JsString(obj.workflowMode)
    }

    override def read(json: JsValue): WorkflowMode = {
      json match {
        case JsString(str) => WorkflowMode.toWorkFlowMode(str)
        case _ =>
          throw DeserializationException("Unable to parse WorkflowMode")
      }
    }
  }

  implicit object StepTypeFormat extends RootJsonFormat[StepType] {
    override def write(obj: StepType): JsValue = {
      JsString(obj.stepType)
    }

    override def read(json: JsValue): StepType = {
      json match {
        case JsString(str) => StepType.toStepType(str)
        case _ =>
          throw DeserializationException("Unable to parse StepType")
      }
    }
  }

  implicit object ScriptTypeFormat extends RootJsonFormat[ScriptType] {
    override def write(obj: ScriptType): JsValue = {
      JsString(obj.scriptType)
    }

    override def read(json: JsValue): ScriptType = {
      json match {
        case JsString(str) => ScriptType.toScriptType(str)
        case _ =>
          throw DeserializationException("Unable to parse StepType")
      }
    }
  }

  implicit object ScriptFileFormat extends RootJsonFormat[ScriptFile] {
    override def write(obj: ScriptFile): JsValue = {
      val fieldNames = List("name", "path")
      val fields = AGU.stringToJsField(fieldNames.head, obj.name) ++
        AGU.stringToJsField(fieldNames(1), obj.path)
      JsObject(fields: _*)
    }

    override def read(json: JsValue): ScriptFile = {
      json match {
        case JsObject(map) =>
          ScriptFile(
            AGU.getProperty[Long](map, "id"),
            AGU.getProperty[String](map, "name"),
            AGU.getProperty[String](map, "path"),
            None)
        case _ => throw DeserializationException("Unable to parse StepType")
      }
    }
  }

  implicit object VariableScopeFormat extends RootJsonFormat[VariableScope] {
    override def write(obj: VariableScope): JsValue = {
      JsString(obj.variableScope)
    }

    override def read(json: JsValue): VariableScope = {
      json match {
        case JsString(str) => VariableScope.toVariableScope(str)
        case _ => throw DeserializationException("Unable to deserialize VariableScope")
      }
    }
  }

  implicit object CumulativeStepExecutionStatusFormat extends RootJsonFormat[CumulativeStepExecutionStatus] {
    override def write(obj: CumulativeStepExecutionStatus): JsValue = {
      JsString(obj.cumulativeStepExecutionStatus)
    }

    override def read(json: JsValue): CumulativeStepExecutionStatus = {
      json match {
        case JsString(str) => CumulativeStepExecutionStatus.toCumulativeStepExecutionStatus(str)
        case _ => throw DeserializationException("Unable to deserialize the CumulativeStepExecutionStatus")
      }
    }
  }

  implicit object StepExecutionStatusFormat extends RootJsonFormat[StepExecutionStatus] {
    override def write(obj: StepExecutionStatus): JsValue = {
      JsString(obj.stepExecutionStatus)
    }

    override def read(json: JsValue): StepExecutionStatus = {
      json match {
        case JsString(str) => StepExecutionStatus.toStepExecutionStatus(str)
        case _ => throw DeserializationException("Unable deserialize the StepExecutionStatus")
      }
    }
  }

  implicit object TaskStatusFormat extends RootJsonFormat[TaskStatus] {
    override def write(obj: TaskStatus): JsValue = {
      JsString(obj.taskStatus)
    }

    override def read(json: JsValue): TaskStatus = {
      json match {
        case JsString(str) => TaskStatus.toTaskStatus(str)
        case _ => throw DeserializationException("Unable to deserialize TaskStatus")
      }
    }
  }

  implicit object TaskTypeFormat extends RootJsonFormat[TaskType] {
    override def write(obj: TaskType): JsValue = {
      JsString(obj.taskType)
    }

    override def read(json: JsValue): TaskType = {
      json match {
        case JsString(str) => TaskType.toTaskType(str)
        case _ => throw DeserializationException("Unable to deserialize the TaskType")
      }
    }
  }

  implicit object StepOrderStrategyFormat extends RootJsonFormat[StepOrderStrategy] {
    override def write(obj: StepOrderStrategy): JsValue = {
      JsString(obj.orderStrategy)
    }

    override def read(json: JsValue): StepOrderStrategy = {
      json match {
        case JsString(str) => StepOrderStrategy.toOrderStrategy(str)
        case _ => throw DeserializationException("Unable to deserialize the StepOrderStrategy")
      }
    }
  }

  implicit object WorkflowExecutionStrategyFormat extends RootJsonFormat[WorkflowExecutionStrategy] {
    override def write(obj: WorkflowExecutionStrategy): JsValue = {
      JsString(obj.executionStrategy)
    }

    override def read(json: JsValue): WorkflowExecutionStrategy = {
      json match {
        case JsString(str) => WorkflowExecutionStrategy.toExecutionStrategy(str)
        case _ => throw DeserializationException("Unable to deserialize WorkflowExecutionStrategy")
      }
    }
  }

  implicit object WorkFlowExecutionStatusFormat extends RootJsonFormat[WorkFlowExecutionStatus] {
    override def write(obj: WorkFlowExecutionStatus): JsValue = {
      JsString(obj.executionStatus)
    }

    override def read(json: JsValue): WorkFlowExecutionStatus = {
      json match {
        case JsString(str) => WorkFlowExecutionStatus.toExecutionStatus(str)
        case _ => throw DeserializationException("Unable to deserialize WorkFlowExecutionStatus")
      }
    }
  }

  implicit object ScopeTypeFormat extends RootJsonFormat[ScopeType] {
    override def write(obj: ScopeType): JsValue = {
      JsString(obj.scopeType)
    }

    override def read(json: JsValue): ScopeType = {
      json match {
        case JsString(str) => ScopeType.toScopeType(str)
        case _ =>
          throw DeserializationException("Unable to parse ScopeType")
      }
    }
  }

  implicit val variableFormat = jsonFormat6(Variable.apply)
  implicit val hostFormat = jsonFormat3(Host.apply)
  implicit val groupFormat = jsonFormat4(Group.apply)
  implicit val inventoryFormat = jsonFormat6(Inventory.apply)

  implicit val taskFormat = jsonFormat5(Task.apply)
  implicit val taskReportFormat = jsonFormat4(TaskReport.apply)
  implicit val nodeReportFormat = jsonFormat5(NodeReport.apply)
  implicit val inventoryExecFormat = jsonFormat6(InventoryExecutionScope.apply)
  implicit val stepInputFormat = jsonFormat2(StepInput.apply)

  implicit object ScriptArgumentFormat extends RootJsonFormat[ScriptArgument] {
    val fieldNames = List("id", "propName", "propValue", "argOrder", "nestedArg", "value")

    // scalastyle:off magic.number
    override def read(json: JsValue): ScriptArgument = {
      json match {
        case JsObject(map) =>
          val mayBeScriptArg = for {
            propName <- AGU.getProperty[String](map, fieldNames(1))
            propValue <- AGU.getProperty[String](map, fieldNames(2))
            argOrder <- AGU.getProperty[Int](map, fieldNames(3))
            value <- AGU.getProperty[String](map, fieldNames(5))
          } yield {
            ScriptArgument(AGU.getProperty[Long](map, fieldNames(0)),
              propName,
              propValue,
              argOrder,
              AGU.getProperty[JsValue](map, fieldNames(4)).map(jsVal => ScriptArgumentFormat.read(jsVal)),
              value)
          }
          mayBeScriptArg match {
            case Some(scriptArgument) => scriptArgument
            case None =>
              logger.warn("Unable to deserialize ScriptArgument,unknown properties found")
              throw DeserializationException("Unable to deserialize ScriptArgument, unknown properties found")
          }
        case _ => throw DeserializationException("Unable to deserialize ScriptArgument, required JsObject")
      }
    }

    override def write(obj: ScriptArgument): JsValue = {
      val fields = AGU.longToJsField(fieldNames(0), obj.id) ++
        AGU.stringToJsField(fieldNames(1), Some(obj.propName)) ++
        AGU.stringToJsField(fieldNames(2), Some(obj.propValue)) ++
        AGU.stringToJsField(fieldNames(3), Some(obj.argOrder.toString)) ++
        AGU.objectToJsValue[ScriptArgument](fieldNames(4), obj.nestedArg, ScriptArgumentFormat) ++
        AGU.stringToJsField(fieldNames(5), Some(obj.value))
      JsObject(fields: _*)
    }

    // scalastyle:on magic.number
  }

  implicit val puppetModuleDefFormat = jsonFormat5(PuppetModuleDefinition.apply)
  implicit val ansibleModuleDefFormat = jsonFormat3(AnsibleModuleDefinition.apply)

  implicit object ModuleFormat extends RootJsonFormat[Module] {
    val fieldNames = List("id", "name", "path", "version", "definition")

    override def read(json: JsValue): Module = {
      json match {
        case JsObject(map) =>
          val definitionObj = if (map.contains("definition")) {
            val obj = map(fieldNames(4))
            if (obj.asJsObject.fields.contains("playBooks")) {
              ansibleModuleDefFormat.read(obj)
            } else {
              puppetModuleDefFormat.read(obj)
            }
          } else {
            logger.warn("Unable to deserialize ModuleDefinition in Module,property 'definition' not found")
            throw DeserializationException("Unable to deserialize ModuleDefinition in Module,property 'definition' not found")
          }
          val mayBeModule = for {
            name <- AGU.getProperty[String](map, "name")
            path <- AGU.getProperty[String](map, "path")
          } yield {
            Module(AGU.getProperty[Long](map, "id"),
              name,
              path,
              AGU.getProperty[String](map, "version"),
              definitionObj)
          }
          mayBeModule match {
            case Some(module) => module
            case None =>
              logger.warn("Unable to deserialize Module , unknown properties found")
              throw DeserializationException("Unable to deserialize Module , unknown properties found")
          }
        case _ => throw DeserializationException("Unable to deserialize Module")
      }
    }

    override def write(obj: Module): JsValue = {
      val definitionFormat = obj.definition match {
        case _: AnsibleModuleDefinition => AGU.objectToJsValue[AnsibleModuleDefinition](fieldNames(4), Some(obj.definition.asInstanceOf[AnsibleModuleDefinition]), ansibleModuleDefFormat)
        case _: PuppetModuleDefinition => AGU.objectToJsValue[PuppetModuleDefinition](fieldNames(4), Some(obj.definition.asInstanceOf[PuppetModuleDefinition]), puppetModuleDefFormat)
      }
      val fields = AGU.longToJsField(fieldNames(0), obj.id) ++
        AGU.stringToJsField(fieldNames(1), Some(obj.name)) ++
        AGU.stringToJsField(fieldNames(2), Some(obj.path)) ++
        AGU.stringToJsField(fieldNames(3), obj.version) ++
        definitionFormat
      JsObject(fields: _*)
    }
  }

  implicit val puppetScriptDependenciesFormat = jsonFormat4(PuppetScriptDependencies.apply)
  implicit val scriptDefinitionFormat = jsonFormat8(ScriptDefinition.apply)
  implicit val ansibleGroupFormat = jsonFormat3(AnsibleGroup.apply)
  implicit val ansiblePlayFormat = jsonFormat13(AnsiblePlay.apply)
  implicit val ansiblePlayBookFormat = jsonFormat5(AnsiblePlayBook.apply)

  implicit object StepFormat extends RootJsonFormat[Step] {
    val fieldNames = List("id", "stepId", "name", "description", "stepType", "scriptDefinition", "input", "scope",
      "executionOrder", "childStep", "report")

    // scalastyle:off magic.number
    override def write(obj: Step): JsValue = {
      val scriptFormat = obj.script match {
        case scriptDef: ScriptDefinition => AGU.objectToJsValue[ScriptDefinition](fieldNames(5), Some(scriptDef), scriptDefinitionFormat)
        case ansiblePlay: AnsiblePlay => AGU.objectToJsValue[AnsiblePlay](fieldNames(5), Some(ansiblePlay), ansiblePlayFormat)
      }
      val fields = AGU.longToJsField(fieldNames.head, obj.id) ++
        AGU.stringToJsField(fieldNames(1), obj.stepId) ++
        AGU.stringToJsField(fieldNames(2), Some(obj.name)) ++
        AGU.stringToJsField(fieldNames(3), obj.description) ++
        AGU.stringToJsField(fieldNames(4), obj.stepType.map(_.stepType)) ++
        scriptFormat ++
        AGU.objectToJsValue[StepInput](fieldNames(6), obj.input, stepInputFormat) ++
        AGU.objectToJsValue[InventoryExecutionScope](fieldNames(7), obj.scope, inventoryExecFormat) ++
        AGU.stringToJsField(fieldNames(8), Some(obj.executionOrder.toString)) ++
        AGU.listToJsValue(fieldNames(9), obj.childStep, StepFormat) ++
        AGU.objectToJsValue[StepExecutionReport](fieldNames(10), obj.report, stepExecFormat)
      JsObject(fields: _*)
    }

    override def read(json: JsValue): Step = {
      json match {
        case JsObject(map) =>
          val scriptObj = if (map.contains("script")) {
            val obj = map(fieldNames(5))
            if (obj.asJsObject.fields.contains("taskList")) {
              ansiblePlayFormat.read(obj)
            } else {
              scriptDefinitionFormat.read(obj)
            }
          } else {
            logger.warn("Unable to deserialize Script in Step,property 'taskList' not found")
            throw DeserializationException("Unable to deserialize  Script,property 'taskList' not found")
          }
          val mayBeStep: Option[Step] = for {
            name <- AGU.getProperty[String](map, fieldNames(2))
            scriptJsVal <- AGU.getProperty[JsValue](map, fieldNames(5))
            executionOrder <- AGU.getProperty[Int](map, fieldNames(8))
          } yield {

            Step(AGU.getProperty[Long](map, fieldNames(0)),
              AGU.getProperty[String](map, fieldNames(1)),
              name,
              AGU.getProperty[String](map, fieldNames(3)),
              AGU.getProperty[String](map, fieldNames(4)).map(StepType.toStepType),
              scriptObj,
              AGU.getProperty[JsValue](map, fieldNames(6)).map(stepInputFormat.read),
              AGU.getProperty[JsValue](map, fieldNames(7)).map(inventoryExecFormat.read),
              executionOrder,
              AGU.getObjectsFromJson[Step](map, "childStep", StepFormat),
              AGU.getProperty[JsValue](map, fieldNames(10)).map(stepExecFormat.read)
            )
          }
          mayBeStep match {
            case Some(step) => step
            case None =>
              logger.warn("Unable to deserialize the Step , found unknown properties")
              throw DeserializationException("Unable to deserialize the Step , found unknown properties")
          }
        case _ => throw DeserializationException("Unable to deserialize Step")
      }
    }

    // scalastyle:on magic.number
  }

  implicit val stepExecFormat = jsonFormat4(StepExecutionReport.apply)
  implicit val workflowExecutionFormat = jsonFormat9(WorkflowExecution.apply)
  implicit val workflowFormat = jsonFormat14(Workflow.apply)
  implicit val pageWorkflowFormat = jsonFormat4(Page[Workflow])
  implicit val pageStepTypeFormat = jsonFormat4(Page[StepType])
  implicit val pageScriptTypeFormat = jsonFormat4(Page[ScriptType])
  implicit val pageScopeTypeFormat = jsonFormat4(Page[ScopeType])
  implicit val pageStringFormat = jsonFormat4(Page[String])
  implicit val scriptContentFormat = jsonFormat4(ScriptContent.apply)

  val workflowRoutes: Route = pathPrefix("workflows") {
    path(LongNumber / "run") { workflowId =>
      post {
        logger.info("Received request to execute workflow, with id - " + workflowId)
            val result = Future {
              val workflow = WorkFlowServiceManagerImpl.getWorkFlow(workflowId)
              if(!workflow.isDefined){
                throw  new Exception(s"Workflow details with id : $workflowId are not available")
              }
              val status = WorkflowDDHandler.getWorkflowStatus(workflowId.toString).getOrElse(WORKFLOW_NOTFOUND)
              status match {
                case WORKFLOW_RUNNING =>
                  throw new Exception("Workflow is already running")
                case _ => WorkFlowServiceManagerImpl.execute(workflow,true)
              }
            }
            onComplete(result) {
              case Success(response) => complete(StatusCodes.OK, s"Request to executed the workflow, with $workflowId initiated..")
              case Failure(exception) =>
                logger.error(s"Error while starting the $workflowId, Reason : ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable execute workflow, Refer logs to se")
            }
        }
      } ~
      path(LongNumber / "status") {
        workflowId =>
        get {
          logger.info("Checking workflow status, with id - " + workflowId)
          val result = Future {
            val workflow = WorkFlowServiceManagerImpl.getWorkFlow(workflowId)
            if(!workflow.isDefined){
              throw  new Exception(s"Workflow details with id : $workflowId are not available")
            }
            WorkflowDDHandler.getWorkflowStatus(workflowId.toString).getOrElse(WORKFLOW_NOTFOUND)
          }
          onComplete(result) {
            case Success(response) => complete(StatusCodes.OK, s"Status of workflow is : $response")
            case Failure(exception) =>
              logger.error(s"Error in retrieving status of the workflow-id $workflowId, Reason : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Failed to get workflow status of workflow $workflowId")
          }
        }
      } ~
    path("step" / "types") {
      get {
        val stepTypes = Future {
          val stepTypesList = StepType.values
          Page[StepType](stepTypesList)
        }
        onComplete(stepTypes) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to get the Step Types list ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Step Types list")
        }
      }
    } ~ path("step" / "scriptTypes") {
      get {
        val scriptTypes = Future {
          val scriptTypesList = ScriptType.values
          Page[ScriptType](scriptTypesList)
        }
        onComplete(scriptTypes) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to get the Script Types list ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Script Types list")
        }
      }
    } ~ path("scope" / "types") {
      get {
        val scopeTypes = Future {
          val scopeTypesList = ScopeType.values
          Page[ScopeType](scopeTypesList)
        }
        onComplete(scopeTypes) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to get the Scope Types list ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Scope Types list")
        }
      }
    } ~ path(LongNumber) { workflowId =>
      get {
        val workflow = Future {
          val mayBeWorkflow = getWorkflow(workflowId)
          mayBeWorkflow match {
            case Some(wkfl) => wkfl
            case None =>
              logger.warn(s"Node not found with ID: $workflowId")
              throw new Exception(s"Node not found with ID: $workflowId")
          }
        }
        onComplete(workflow) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to get the Workflow list ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get workflow list")
        }
      }
    } ~ path(LongNumber / "logs") { workflowId =>
      get {
        parameter("start".as[Int]) { filePointer =>
          val logs = Future {
            for {
              workflow <- getWorkflow(workflowId)
              execution <- workflow.execution
            } yield {
              val logs = execution.logs
              val size = logs.size
              val start = if (filePointer == 0) 1 else filePointer
              val logTail = if (start < size) logs.slice(start - 1, size) else List.empty[String]
              Page[String](logTail)
            }
          }
          onComplete(logs) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to get the Logs ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get Logs")
          }
        }
      }
    } ~ path(LongNumber / "inventory") { workflowId =>
      put {
        entity(as[Inventory]) { inventory =>
          val inv = Future {
            addInventory(workflowId, inventory)
            "Inventory added successfully"
          }
          onComplete(inv) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to add Inventory ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to add Inventory")
          }
        }
      }
    } ~ path(LongNumber / "inventory") { workflowId =>
      get {
        val inventory = Future {
          logger.info(s"Retrieving inventory for workflow[ $workflowId ]")
          val mayBeWorkflow = getWorkflow(workflowId)
          mayBeWorkflow match {
            case Some(workflow) =>
              val mayBeExecution = workflow.execution
              mayBeExecution match {
                case Some(execution) => execution.inventory
                case None => throw new RuntimeException(s"No inventory built for execution of workflow [ ${workflow.name} ]")
              }
            case None => throw new RuntimeException(s"No workflow with id [ $workflowId ] found")
          }
        }
        onComplete(inventory) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to retrieve Inventory ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to retrieve Inventory")
        }
      }
    } ~ path(LongNumber / "history") { workflowId =>
      get {
        val executionHistory = Future {
          logger.info("Retrieving workflow execution history")
          val mayBeWorkflow = getWorkflow(workflowId)
          mayBeWorkflow.map(_.executionHistory)
        }
        onComplete(executionHistory) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to retrieve workflow execution history ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to retrieve workflow execution history")
        }
      }
    } ~ path(LongNumber / "history" / LongNumber) { (workflowId, executionId) =>
      get {
        val execution = Future {
          val mayBeExecution = WorkflowExecution.fromNeo4jGraph(executionId)
          mayBeExecution match {
            case Some(exc) => exc
            case None =>
              logger.warn(s"Workflow Execution with ID : $executionId is Not Found")
              throw new NotFoundException(s"WorkFlow Execution with ID : $executionId is Not Found")
          }
        }
        onComplete(execution) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to retrieve workflow execution ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to retrieve workflow execution ")
        }
      }
    } ~ path(LongNumber / "content") { workflowId =>
      post {
        entity(as[ScriptContent]) { content =>
          val moduleContent = Future {
            logger.info("Reading workflow module content")
            readContent(content)
          }
          onComplete(moduleContent) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to read workflow module content ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to read workflow module content ")
          }
        }
      }
    } ~ path(LongNumber / "export") { workflowId =>
      val mayBeWorkflow = getWorkflow(workflowId)
      val mayBeZipFile = for {
        workflow <- mayBeWorkflow
        module <- workflow.module
      } yield {
        val modulePath = module.path
        val workflowZipPath = exportWorkflow(modulePath, workflow)
        val workflowZip = new File(workflowZipPath)
        workflowZip
      }
      mayBeZipFile match {
        case Some(zipFile) => getFromFile(zipFile)
        case None =>
          logger.error(s"Workflow with id [ $workflowId ] or Module not found")
          complete(StatusCodes.BadRequest, s"Workflow with id [ $workflowId ] or Module not found")
      }
    } ~ path(LongNumber / "clone") { workflowId =>
      post {
        entity(as[String]) { name =>
          val workflow = Future {
            logger.info(s"Received request for cloning workflow with id - $workflowId and name $name")
            cloneWorkflow(workflowId, name)
          }
          onComplete(workflow) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to clone workflow ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to clone workflow")
          }
        }
      }
    } ~ path("published") {
      get {
        val publishedWorkflows = Future {
          val wfLabel = Workflow.labelName
          val nodesList = Neo4jRepository.getNodesByLabel(wfLabel)
          val workflows = nodesList.flatMap(node => Workflow.fromNeo4jGraph(node.getId))
          val workflowsList = workflows.filter(_.publishedInventory.nonEmpty)
          Page[Workflow](workflowsList)
        }
        onComplete(publishedWorkflows) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to retrieve published workflows List. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to retrieve published workflows List.")
        }
      }
    } ~ get {
      val workflows = Future {
        val nodesList = Neo4jRepository.getNodesByLabel(Workflow.labelName)
        val workflowList = nodesList.flatMap(node => Workflow.fromNeo4jGraph(node.getId))
        Page[Workflow](workflowList)
      }
      onComplete(workflows) {
        case Success(response) => complete(StatusCodes.OK, response)
        case Failure(exception) =>
          logger.error(s"Unable to get the Workflow list ${exception.getMessage}", exception)
          complete(StatusCodes.BadRequest, "Unable to get workflow list")
      }
    } ~ put {
      entity(as[Workflow]) { workflow =>
        val createWorkflow = Future {
          workflow.mode match {
            case Some(AGENT_LESS) => addWorkflow(workflow)
            case _ => workflow.toNeo4jGraph(workflow)
          }
          logger.info(s"Created workflow with id - ${workflow.id}")
          "Successfully added workflow"
        }
        onComplete(createWorkflow) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to create workflow ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to create workflow")
        }
      }
    } ~ post {
      entity(as[Workflow]) { workflow =>
        val updateWorkflow = Future {
          if (workflow.id.isEmpty) {
            throw new RuntimeException("Missing Workflow id")
          }
          workflow.toNeo4jGraph(workflow)
          "Workflow updated successfully"
        }
        onComplete(updateWorkflow) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to update workflow ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to update workflow")
        }
      }
    } ~ path("published" / LongNumber) { workflowId =>
      post {
        entity(as[List[Variable]]) { variables =>
          val updatedWorkflow = Future {
            for {
              workflow <- getWorkflow(workflowId)
              inventory <- workflow.publishedInventory
            } yield {
              val updatedGroups = inventory.groups.map { group =>
                val updatedVars = group.variables.flatMap { variable =>
                  variables.map { variab =>
                    if (variab.name.equals(variable.name)) {
                      variab.copy(value = variable.value)
                    } else {
                      variable
                    }
                  }
                }
                group.copy(variables = updatedVars)
              }
              val updatedInventory = inventory.copy(groups = updatedGroups)
              val marshalledInv = addJsonToInventory(updatedInventory)
              addInventory(workflowId, marshalledInv)
            }
            getWorkflow(workflowId)
          }
          onComplete(updatedWorkflow) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to update inventory ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to update the published inventory with ID $workflowId")
          }
        }
      }
    }
  }

  val stepServiceRoutes: Route = pathPrefix("workflow" / LongNumber) { workflowId =>
    path("step" / LongNumber) { stepId =>
      get {
        val step = Future {
          val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(Step.labelName, "id", stepId)
          mayBeNode.flatMap(node => Step.fromNeo4jGraph(node.getId))
        }
        onComplete(step) {
          case Success(response) =>
            if (response.nonEmpty)
              complete(StatusCodes.OK, response)
            else
              complete(StatusCodes.NoContent, s"No Step found with workflow $workflowId and step $stepId")
          case Failure(exception) =>
            logger.error(s"Unable to fetch step ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, s"Unable to fetch step entity with workflowId : $workflowId and stepId : $stepId ")
        }
      }
    } ~ path("step" / LongNumber / "status") { stepId =>
      get {
        val report = Future {
          val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(Step.labelName, "id", stepId)
          val mayBeStep = mayBeNode.flatMap(node => Step.fromNeo4jGraph(node.getId))
          mayBeStep.map(step => step.report)
        }
        onComplete(report) {
          case Success(response) =>
            if (response.nonEmpty)
              complete(StatusCodes.OK, response)
            else
              complete(StatusCodes.NoContent, s"No Step found with workflow $workflowId and step $stepId")
          case Failure(exception) =>
            logger.error(s"Unable to fetch Execution report ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, s"Unable to get Report with workflow $workflowId and step $stepId")
        }
      }
    }
  } ~ pathPrefix("workflows") {
    path(LongNumber / "status") { workflowId =>
      parameter("start") { start =>
        val workflowExecution = Future {
          val workFlowExecution = getRunningWorkflowExecution(workflowId)
          workFlowExecution match {
            case Some(workflowExec) =>
              for {
                workflow <- Workflow.fromNeo4jGraph(workflowId)
                execution <- workflow.execution
              } yield {
                val from = if (start.toInt == 0) 1 else start.toInt
                if (from < execution.logs.size) {
                  execution.copy(logs = execution.logs.slice(from, execution.logs.size))
                } else {
                  execution.copy(logs = List.empty[String])
                }
              }
            case None =>
              logger.warn(s"No workflow found with Id: $workflowId")
              throw new Exception(s"No running workflow found with Id : $workflowId")
          }
        }
        onComplete(workflowExecution) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to fetch the WorkflowExecution with given WorkflowID : $workflowId", exception)
            complete(StatusCodes.BadRequest, s"No Running Workflow is found with ID : $workflowId")
        }
      }
    } ~ path(LongNumber / "publish") { workflowId =>
      put {
        entity(as[Inventory]) { inventory =>
          val publishedInventory = Future {
            for {
              workflow <- getWorkflow(workflowId)
            } yield {
              val marshlledInventory = addJsonToInventory(inventory)
              workflow.copy(publishedInventory = Some(marshlledInventory))
              workflow.toNeo4jGraph(workflow)
              addInventory(workflowId, inventory)
              "Workflow is published"
            }
          }
          onComplete(publishedInventory) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to publish the inventory ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to publish the inventory")
          }
        }
      } ~ get {
        val publishedInventory = Future {
          for {
            workflow <- getWorkflow(workflowId)
          } yield {
            workflow.publishedInventory
          }
        }
        onComplete(publishedInventory) {
          case Success(response) => complete(StatusCodes.OK, response)
          case Failure(exception) =>
            logger.error(s"Unable to fetch Published Inventory ${exception.getMessage()}", exception)
            complete(StatusCodes.BadRequest, "Unable to fetch Published Inventory")
        }
      }
    } ~ path(LongNumber) { workflowId =>
      delete {
        val isDeleted = Future {
          for {
            workflow <- getWorkflow(workflowId)
          } yield {
            workflowCompleted(workflow, WorkFlowExecutionStatus.INTERRUPTED)
            val execHistory = workflow.executionHistory
            execHistory.foreach(workflowExecution => workflowExecution.id.foreach(id => Neo4jRepository.deleteEntity(id)))
            val steps = workflow.steps.foreach(step => step.id.foreach(id => Neo4jRepository.deleteEntity(id)))
            workflow.module.map { module =>
              val path = FilenameUtils.getFullPath(module.path)
              val file = new File(path)
              CommonFileUtils.deleteDirectory(file)
              true
            }
          }
        }
        onComplete(isDeleted) {
          case Success(response) =>
            complete(StatusCodes.OK, "Workflow deleted succesfully")
          case Failure(exception) =>
            logger.error(s"Unable to delete workflow ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, s"Unable to delete workflow with id $workflowId")
        }
      }
    }
  }

  def getRunningWorkflowExecution(workflowId: Long): Option[WorkflowExecution] = {
    if (currentWorkflows.contains(workflowId)) {
      currentWorkflows(workflowId).workflow.execution
    } else {
      None
    }
  }

  def addJsonToInventory(inventory: Inventory): Inventory = {
    val inventoryJsVal = inventoryFormat.write(inventory)
    inventory.copy(json = inventoryJsVal.toString)
  }

  def workflowCompleted(workflow: Workflow, workFlowExecutionStatus: WorkFlowExecutionStatus): Unit = {
    for {
      workflowExec <- workflow.execution
    } yield {
      val stoppedWorkflowExec = workflowExec.copy(status = Some(workFlowExecutionStatus))
      stoppedWorkflowExec.toNeo4jGraph(stoppedWorkflowExec)
      ansibleWorkflowProcessor.stopWorkflow(workflow)
      workflow.id.map(id => currentWorkflows.remove(id))
    }
  }

  def getWorkflow(workflowId: Long): Option[Workflow] = {
    val mayBeWorkflow = Workflow.fromNeo4jGraph(workflowId)
    mayBeWorkflow.map { w =>
      val steps = w.steps
      val sortedStepsByTaskId = steps.map { step =>
        val ansiblePlay = step.script.asInstanceOf[AnsiblePlay]
        val tasks = ansiblePlay.taskList
        val sortedTasks = tasks.sortBy(_.id)
        ansiblePlay.copy(taskList = sortedTasks)
        step.copy(script = ansiblePlay)
      }
      val sortedStepsByExcOrder = sortedStepsByTaskId.sortBy(_.executionOrder)
      val workflow = w.copy(steps = sortedStepsByExcOrder)
      workflow
    }
  }

  def addInventory(workflowId: Long, inventory: Inventory): Unit = {
    val mayBeWorkflow = getWorkflow(workflowId)
    mayBeWorkflow match {
      case Some(wkfl) =>
        val mayBeExecution = wkfl.execution
        val (workflow, newExecution) = mayBeExecution match {
          case Some(exc) =>
            exc.status match {
              case Some(WorkFlowExecutionStatus.STARTED) =>
                throw new RuntimeException(s"Workflow with id [ $workflowId ] is currently running, cannot update inventory")
              case Some(WorkFlowExecutionStatus.FAILED) | Some(WorkFlowExecutionStatus.SUCCESS) =>
                (addWorkflowExecutionHistory(wkfl, exc), None)
              case _ => (wkfl, Some(exc))
            }
          case None => (wkfl, None)
        }
        val mayBeId = newExecution.flatMap(_.id)
        mayBeId match {
          case Some(id) =>
            inventory.toNeo4jGraph(inventory)
            mayBeExecution.map { exc =>
              exc.copy(inventory = Some(inventory))
              exc.toNeo4jGraph(exc)
            }
          case None =>
            val exc = WorkflowExecution(Some(inventory))
            workflow.copy(execution = Some(exc))
            workflow.toNeo4jGraph(workflow)
        }
      case None => throw new RuntimeException(s"No workflow with id [ $workflowId ] found")
    }
  }

  def addWorkflowExecutionHistory(workflow: Workflow, execution: WorkflowExecution): Workflow = {
    val wkfl = workflow.copy(executionHistory = execution :: workflow.executionHistory)
    wkfl.toNeo4jGraph(wkfl)
    wkfl
  }

  def readContent(content: ScriptContent): Option[ScriptContent] = {
    val mayBePath = buildPath(content.path, content.scriptType)
    mayBePath.map { path =>
      val fileToRead = path.concat(File.separator).concat(content.name)
      val f = new File(fileToRead)
      if (!f.exists()) {
        throw new RuntimeException(s"File $fileToRead not found")
      }
      try {
        val fileContent = CommonFileUtils.readFileToString(f)
        content.copy(content = fileContent)
      } catch {
        case e: IOException => throw new RuntimeException(s"Cannot read $fileToRead")
      }
    }
  }

  def buildPath(path: String, scriptType: ScriptType): Option[String] = {
    scriptType match {
      case ScriptType.Script =>
        val targetFile = new StringBuilder
        targetFile.append(FilenameUtils.getFullPathNoEndSeparator(path)).append(File.separator).append("files")
        Some(targetFile.toString)
      case _ => None
    }
  }

  def exportWorkflow(modulePath: String, workflow: Workflow): String = {
    val workflowZipPath = Constants.TEMP_DIR_LOC + File.separator + System.currentTimeMillis() + ""
    val workflowName = workflow.name
    logger.info(s"Exporting workflow [ $workflowName ] to $workflowZipPath")
    val dir = new File(workflowZipPath)
    dir.mkdirs
    val inventoryFile = new File(modulePath.concat(File.separator).concat("inventory_cjo.json"))
    for {
      exc <- workflow.execution
      inv <- exc.inventory
    } yield {
      try {
        CommonFileUtils.writeStringToFile(inventoryFile, inv.json)
      } catch {
        case e: IOException =>
          logger.error(s"Failed to export inventory for workflow [ $workflowName ]")
      }
    }
    val workflowZipFile = new StringBuilder(workflowZipPath)
      .append(File.separator).append(workflowName).append(".zip")
    try {
      zipWorkflow(
        FilenameUtils.getFullPathNoEndSeparator(modulePath), workflowZipFile.toString
      )
    } catch {
      case e: IOException => logger.error(e.getMessage, e)
        throw new RuntimeException(s"Error while exporting workflow [ $workflowName ]")
    }
    inventoryFile.delete
    workflowZipFile.toString
  }

  def zipWorkflow(source: String, dest: String): Unit = {
    val sourceDir = new File(source)
    val fos = new FileOutputStream(dest)
    val zos = new ZipOutputStream(fos)
    val fileList = getFileList(sourceDir)
    fileList.foreach { filePath =>
      val ze = new ZipEntry(filePath.substring(sourceDir.getAbsolutePath.length + 1, filePath.length))
      zos.putNextEntry(ze)
      val fis = new FileInputStream(filePath)
      val size = 1024
      val buffer = new Array[Byte](size)
      var len = 0
      while ( {
        len = fis.read(buffer)
        len > 0
      }) {
        zos.write(buffer, 0, len)
      }
      zos.closeEntry()
      fis.close()
    }
    zos.close()
    fos.close()
  }

  def getFileList(dir: File, fileList: List[String] = List.empty[String]): List[String] = {
    val files = dir.listFiles()
    files.foldLeft(fileList) { (list, file) =>
      if (file.isFile) file.getAbsolutePath :: list else getFileList(file, list)
    }
  }

  def cloneWorkflow(workflowId: Long, wfName: String): Option[Workflow] = {
    val existingWorkflow = getWorkflow(workflowId)
    for {
      workflow <- existingWorkflow
      module <- workflow.module
    } yield {
      val modulePath = module.path
      val workflowZipPath = exportWorkflow(modulePath, workflow)
      val workflowZip = new File(workflowZipPath)
      //TODO add module
      val newModule = module
      val existingPlaybooks = workflow.playBooks
      val newPlayBooks = existingPlaybooks.map { playBook =>
        AnsiblePlayBook(None, playBook.name, None, List.empty[AnsiblePlay], List.empty[Variable])
      }
      val newWorkflow = Workflow(wfName, module, newPlayBooks)
      addWorkflow(newWorkflow)
      newWorkflow
    }
  }

  def addWorkflow(workflow: Workflow): Unit = {
    val newWorkflow = workflow.copy(mode = Some(WorkflowMode.toWorkFlowMode("AGENT_LESS")))
    val workflowNode = newWorkflow.toNeo4jGraph(newWorkflow)
    val mayBeWorkflow = Workflow.fromNeo4jGraph(workflowNode.getId)
    for {
      ansibleWorkflow <- mayBeWorkflow
      module <- ansibleWorkflow.module
      moduleId <- module.id
      newModule <- module.fromNeo4jGraph(moduleId)
    } yield {
      val modulePath = newModule.path
      val playbooks = ansibleWorkflow.playBooks
      playbooks.foreach { playbook =>
        val playbookName = playbook.name match {
          case Some(name) => name
          case None => throw new Exception("play book name cannot be empty")
        }
        val newPlayBook = playbook.copy(path = Some(modulePath.concat(File.separator).concat(playbookName)))
        logger.info(s"Parsing ansible playbook [$playbookName] at module path [$modulePath]")
        //TODO parse  playbook
        newPlayBook.toNeo4jGraph(newPlayBook)
        val plays = newPlayBook.playList
        plays.foreach { play =>
          play.copy(language = Some(ScriptType.Ansible))
          val name = play.name match {
            case Some(playName) => playName
            case None => throw new Exception("play doesnt have a name")
          }
          val step = Step(name, play)
          createStep(ansibleWorkflow, step)
        }
      }
    }
  }

  def createStep(workflow: Workflow, step: Step): Unit = {
    val id = if (step.stepId.isEmpty) {
      Some(getRandomString)
    } else {
      step.stepId
    }
    val newStep: Step = step.copy(stepId = id)
    val childNode = newStep.toNeo4jGraph(newStep)
    val stepId = childNode.getId
    workflow.id.foreach { workflowId =>
      Neo4jRepository.createRelationship(stepId, workflowId, "steps")
    }
    val mayBeLastStep = findLastStep(workflow.steps)
    for {
      lastStep <- mayBeLastStep
      lastStepId <- lastStep.id
    } yield {
      Neo4jRepository.createRelationship(stepId, lastStepId, "childSteps")
    }
  }

  def getRandomString: String = {
    val size = 14
    Random.alphanumeric.take(size).mkString
  }

  def findLastStep(steps: List[Step]): Option[Step] = {
    steps.headOption.flatMap { step =>
      if (step.childStep.nonEmpty) findLastStep(step.childStep) else Some(step)
    }
  }

  val appsettingRoutes: Route = pathPrefix("config") {
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
  val discoveryRoutes: Route = pathPrefix("discover") {
    path("site") {
      withRequestTimeout(1.minutes) {
        put {
          entity(as[Site1]) { site =>
            val buildSite = Future {
              val savedSite = populateInstances(site)
              savedSite.id.foreach(siteId => SharedSiteCache.putSite(siteId.toString, savedSite))
              savedSite
            }
            onComplete(buildSite) {
              case Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case Failure(exception) =>
                logger.error(s"Unable to save the Site with : ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to save the Site.")
            }
          }
        }
      }
    } ~ path("site" / "save" / LongNumber) { siteId =>
      withRequestTimeout(2.minutes) {
        put {
          val futureSite = SharedSiteCache.getSite(siteId.toString)
          val siteResponse = futureSite.map {
              case Some(site) =>
                site.toNeo4jGraph(site)
                indexSite(site)
              case None => throw new NotFoundException(s"Site with id : $siteId is not found!")
          }
          onComplete(siteResponse) {
            case Success(response) => complete(StatusCodes.OK, "Site saved successfully!")
            case Failure(exception) =>
              logger.error(s"Unable to save the site ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to save the site")
          }
        }
      }
    } ~ path("site" / LongNumber) {
      siteId =>
        get {
          val siteObj = SharedSiteCache.getSite(siteId.toString)
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
          val futureSite = SharedSiteCache.getSite(siteId.toString)
          val tags = futureSite.map {
              case Some(site) =>
                site.instances.flatMap(instance => instance.tags.filter(tag => tag.key.equalsIgnoreCase(Constants.NAME_TAG_KEY)))
              case None =>
                logger.warn(s"Site Entity with ID : $siteId is Not Found")
                throw new NotFoundException(s"Site Entity with ID : $siteId is Not Found")
          }
          onComplete(tags) {
            case Success(response) => complete(StatusCodes.OK, response)
            case Failure(exception) =>
              logger.error(s"Unable to get the Tags with Given Site ID : $siteId  ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get tags")
          }
      }
    } ~ path("keypairs" / LongNumber) { siteId =>
      get {
        val futureSite = SharedSiteCache.getSite(siteId.toString)
        val listOfKeyPairs = futureSite.map {
            case Some(site) =>
              val keyPairs = site.instances.flatMap { instance =>
                instance.sshAccessInfo.map(x => x.keyPair)
              }
              Page[KeyPairInfo](keyPairs)
            case None =>
              logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
              Page[KeyPairInfo](List.empty[KeyPairInfo])
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
                  site.filters, site.loadBalancers, site.scalingGroups, site.groupsList, List.empty[Application], site.groupBy, site.scalingPolicies))
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
          val filters = siteFilter.filters
          val filteredSite = populateFilteredInstances(siteId, filters)
          onComplete(filteredSite) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to populate Filtered Instances; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to populated Filtered Instances")
          }
        }
      }
    } ~ path("keypairs" / LongNumber) { siteId =>
      put {
        entity(as[Multipart.FormData]) { formData =>
          val uploadKeyPairsResult = Future {
            val dataMap = formData.asInstanceOf[FormData.Strict].strictParts.foldLeft(Map.empty[String, String])((accum, strict) => {
              val name = strict.getName()
              val value = strict.entity.getData().decodeString("UTF-8")
              val mayBeFile = strict.filename
              logger.debug(s"--- $name  -- $value -- $mayBeFile")

              mayBeFile match {
                case Some(fileName) => accum + ((name, value))
                case None =>
                  if (name.equalsIgnoreCase("userName") || name.equalsIgnoreCase("passPhase")) {
                    accum + ((name, value))
                  }
                  else {
                    accum
                  }
              }
            })
            val sshKeyContentInfo = SSHKeyContentInfo(dataMap)
            val siteNode = Site1.fromNeo4jGraph(siteId)
            siteNode match {
              case Some(site) =>
                val instanceFromSite = site.instances
                instanceFromSite.foreach {
                  instance =>
                    val accessInfo = instance.sshAccessInfo
                    accessInfo.foreach { info =>
                      val persistedKeyName = info.keyPair.keyName
                      val keyMaterials = sshKeyContentInfo.keyMaterials
                      if (keyMaterials.nonEmpty) {
                        val keySetMap = keyMaterials.keySet
                        keySetMap.foreach { sshKeyMaterialEntry =>
                          if (!sshKeyMaterialEntry.equals("userName") && !sshKeyMaterialEntry.equals("passPhrase")) {
                            val sshKeyData = keyMaterials.get(sshKeyMaterialEntry)
                            if (persistedKeyName.equalsIgnoreCase(sshKeyMaterialEntry)) {
                              val keyFilePath = sshKeyData.map(keyData => getSshKeyFilePath(instance, persistedKeyName, sshKeyMaterialEntry, keyData))
                              val user = keyMaterials.filterKeys(key => key.equals("username") &&
                                !keyMaterials(key).equalsIgnoreCase("undefined")).get("username")
                              val userName = user.getOrElse("ubuntu") //TODO: check if needed to assign ubuntu or not
                              val passPhrase = keyMaterials.filterKeys(key => key.equals("passPhrase") && !keyMaterials(key).equalsIgnoreCase("undefined")
                                && keyMaterials(key).nonEmpty).get("passPhrase")
                              val keyPairInfoUpdated: KeyPairInfo = info.keyPair.copy(keyMaterial = sshKeyData, filePath = keyFilePath,
                                status = KeyPairStatus.toKeyPairStatus("UPLOADED"), defaultUser = Some(userName))
                              val sSHAccessInfoUpdated: SSHAccessInfo = info.copy(keyPair = keyPairInfoUpdated, userName = Some(userName),
                                port = accessInfo.flatMap(info => info.port))
                              val instanceObj = instance.copy(sshAccessInfo = Some(sSHAccessInfoUpdated))
                              instanceObj.toNeo4jGraph(instanceObj)
                            } else {
                              val user = keyMaterials.filterKeys(key => key.equals("userName") && !keyMaterials(key).equalsIgnoreCase("undefined") &&
                                keyMaterials(key).nonEmpty).get("username")
                              val userName = user.getOrElse("ubuntu")
                              val keyFilePath = sshKeyData.map(keyData => getSshKeyFilePath(instance, persistedKeyName, sshKeyMaterialEntry, keyData))
                              val passPhrase = keyMaterials.filterKeys(key => key.equals("passPhrase") && !keyMaterials(key).equalsIgnoreCase("undefined") &&
                                keyMaterials(key).nonEmpty).get("passPhrase")
                              val persistedUserName = accessInfo.flatMap(info => info.userName)
                              val sshUserName = persistedUserName.getOrElse("ubuntu")
                              val KeyPairInfoUpdated = KeyPairInfo(Some(1), sshKeyMaterialEntry, None, sshKeyData, keyFilePath,
                                KeyPairStatus.toKeyPairStatus("UPLOADED"), Some(userName), passPhrase)
                              val sSHAccessInfoUpdated: SSHAccessInfo = info.copy(keyPair = KeyPairInfoUpdated, userName = Some(sshUserName))
                              val instanceObj = instance.copy(sshAccessInfo = Some(sSHAccessInfoUpdated))
                              instanceObj.toNeo4jGraph(instanceObj)
                            }
                          } else {
                            logger.info("Username and Passphrase not matched")
                          }
                        }
                      } else {
                        logger.info("Keymaterials empty")
                      }
                    }
                }
              case None =>
                throw new NotFoundException(s"Site Entity with ID : $siteId is Not Found")
            }
            "Uploaded Key pair Info successfully"
          }
          onComplete(uploadKeyPairsResult) {
            case Success(page) => complete(StatusCodes.OK, page)
            case Failure(ex) =>
              logger.error(s"Failed to update Keys, Message: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Failed to update Keys")
          }
        }
      }
    } ~ path("sites" / LongNumber / "topology") { siteId =>
      post {
        val siteTopology = Future {
          val siteOption = Site1.fromNeo4jGraph(siteId)
          siteOption.map { site =>

            val softwareLabel: String = "SoftwaresTest2"
            val nodesList = Neo4jRepository.getNodesByLabel(softwareLabel)
            val softwares = nodesList.flatMap(node => Software.fromNeo4jGraph(node.getId))
            val topology = new Topology(site)
            val sshBaseStrategy = new SSHBasedStrategy(topology, softwares, true)
            //TODO: InstanceGroup & Application save
            val topologyResult = sshBaseStrategy.getTopology

            //Saving the Site with collected instance details
            site.toNeo4jGraph(topologyResult.site)
          }
        }
        onComplete(siteTopology) {
          case Success(successResponse) => successResponse match {
            case Some(resp) => complete(StatusCodes.OK, "Saved the site topology")
            case None => complete(StatusCodes.BadRequest, "Unable to find topology for the Site")
          }
          case Failure(ex) =>
            logger.error(s"Unable to find topology; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, "Unable to find topology for the Site")
        }
      }
    }
  }

  val esServiceRoutes: Route = pathPrefix("es") {
    path("search") {
      post {
        entity(as[EsSearchQuery]) { esQuery =>
          val response = Future {
            EsManager.boolQuery(esQuery)
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
            case Failure(exception) =>
              logger.error(s"Unable to get the documents ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get the documents")
          }
        }
      }
    } ~ path("mappings") {
      post {
        entity(as[EsSearchQuery]) {
          esSerachQuery =>
            val response = Future {
              if (esSerachQuery.types.nonEmpty) {
                EsManager.fieldMappings(esSerachQuery.index, esSerachQuery.types.head)
              } else {
                throw new Exception("Search types should not be empty")
              }
            }
            onComplete(response) {
              case Success(reponseMessage) => complete(StatusCodes.OK, reponseMessage)
              case Failure(exception) =>
                logger.error(s"Unable to get mappings ${exception.getMessage}", exception)
                complete(StatusCodes.OK, "Unable to get mappings")
            }
        }
      }
    }
  }
  val pluginServiceRoutes = pathPrefix("plugins") {
    get {
      val plugins = Future {
        val plugInNodes = Neo4jRepository.getNodesByLabel(PlugIn.labelName)
        plugInNodes.map { node =>
          PlugIn.fromNeo4jGraph(node.getId)
        }
      }
      onComplete(plugins) {
        case Success(response) => complete(StatusCodes.OK, response)
        case Failure(exception) =>
          logger.error(s"Unable to get the plugins ${exception.getMessage}", exception)
          complete(StatusCodes.BadRequest, "Unable to get the plugins")
      }
    }
  }

  implicit val pageModuleFormat = jsonFormat4(Page[Module])
  // scalastyle:off method.length
  def adminRoute: Route = pathPrefix("admin") {
    path("module" / "create") {
      put {
        entity(as[Multipart.FormData]) { formData =>
          val createModule = Future {
            val tempPath = Constants.TEMP_DIR_LOC + File.separator + System.currentTimeMillis + ""
            logger.info(s"Saving module zip to temp location - $tempPath")
            val dir = new File(tempPath)
            dir.mkdirs
            val (mayBeModuleName, mayBeFile) = formData.asInstanceOf[FormData.Strict].strictParts
              .foldLeft(None: Option[String], None: Option[File]) { (moduleNameAndFile, strict) =>
                val (moduleName, file) = moduleNameAndFile
                val name = strict.getName()
                val value = strict.entity.getData().decodeString("UTF-8")
                val mayBeFileName = strict.filename
                val m = if (name.equals("userName")) Some(value) else moduleName
                val f = if (name.equals("module")) mayBeFileName.map(f => setFile(value, f, tempPath)) else file
                (m, f)
              }

            mayBeFile.map(file => buildModule(mayBeModuleName, file))
          }
          onComplete(createModule) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to create module. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to create module")
          }
        }
      }
    } ~ path("module" / Segment) { moduleName =>
      get {
        val module = Future {
          val nodesList = Neo4jRepository.getNodesByLabel(Module.labelName)
          val listOfModules = nodesList.flatMap(node => Module.fromNeo4jGraph(node.getId))
          listOfModules.find(module => module.name.equals(moduleName))
        }
        onComplete(module) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to get module with name $moduleName. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, s"Unable to get module with name $moduleName ")
        }
      }
    } ~ path("module" / "list") {
      get {
        val moduleList = Future {
          val nodesList = Neo4jRepository.getNodesByLabel(Module.labelName)
          val listOfModules = nodesList.flatMap(node => Module.fromNeo4jGraph(node.getId))
          val filteredModules = listOfModules.filter { module =>
            module.definition.isInstanceOf[AnsibleModuleDefinition]
          }
          Page[Module](listOfModules)
        }
        onComplete(moduleList) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to Retrieve Module List. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to Module ImageInfo List.")
        }
      }
    }
  }
  // scalastyle:on method.length

  def setFile(value: String, fileName: String, tempPath: String): File = {
    val file = new File(tempPath.concat(File.separator).concat(fileName))
    CommonFileUtils.writeStringToFile(file, value)
    file
  }

  def buildModule(mayBeModuleName: Option[String], moduleZip: File): Module = {
    val playBooks = scala.collection.mutable.ListBuffer[ScriptFile]()
    val moduleZipName = FilenameUtils.getBaseName(moduleZip.getName)
    val moduleName = mayBeModuleName.getOrElse(moduleZipName)
    logger.info(s"Building ansible project for $moduleName")
    val modulePath = getModulePath(moduleName)
    if (new File(modulePath).exists()) {
      throw new RuntimeException(s"Module $moduleName already exists.")
    }
    val zin = new ZipInputStream(new FileInputStream(moduleZip))
    val outDir = new File(modulePath)
    var entry: Option[ZipEntry] = None
    try {
      while ({
        entry = Option(zin.getNextEntry)
        entry.isDefined
      }) {
        entry.foreach { zipEntry =>
          val name = zipEntry.getName
          if (zipEntry.isDirectory) {
            mkdirs(outDir, name)
          } else {
            val mayBeDir = getDir(name)
            mayBeDir.foreach(dir => mkdirs(outDir, dir))
            val file = new File(outDir, name)
            if (isFileAPlayBook(name)) {
              playBooks += ScriptFile(file)
            }
            extractFile(zin, file)
          }
        }
      }
    } finally {
      zin.close()
    }
    val path = modulePath.concat(File.separator).concat(moduleZipName)
    val definition = AnsibleModuleDefinition(None, playBooks.toList, List.empty[ScriptFile])
    Module(None, moduleName, path, None, definition)
  }

  def extractFile(in: ZipInputStream, file: File): Unit = {
    val size = 4 * 1024
    val buffer = new Array[Byte](size)
    val fos = new FileOutputStream(file)
    var count = -1
    try {
      while ({
        count = in.read(buffer)
        count != -1
      }) {
        fos.write(buffer, 0, count)
      }
    } finally {
      fos.close()
    }
  }

  def isFileAPlayBook(name: String): Boolean = {
    val ext = FilenameUtils.getExtension(name)
    ("yml".equals(ext) || "yaml".equals(ext)) && !name.contains(AnsibleConstants.AnsiblePlayRoles)
  }

  def getDir(name: String): Option[String] = {
    val s = name.lastIndexOf(File.separatorChar)
    if (s == -1) None else Some(name.substring(0, s))
  }

  def mkdirs(outDir: File, path: String): Unit = {
    val d = new File(outDir, path)
    if (!d.exists()) d.mkdirs
  }

  def getModulePath(moduleName: String): String = {
    val ansibleProjHome = getSettingFor("ansible.projects.home")
    val moduleDirPath = ansibleProjHome.concat(File.separator).concat(moduleName)
    moduleDirPath
  }

  def getSettingFor(key: String): String = {
    val nodesList = Neo4jRepository.getNodesByLabel(AppSettings.labelName)
    val listOfAppSettings = nodesList.flatMap(node => AppSettings.fromNeo4jGraph(node.getId))
    val appSettings = listOfAppSettings.headOption
    val settings = appSettings.map(_.settings).getOrElse(Map.empty[String, String])
    val value = settings.get(key)
    value match {
      case Some(setting) => setting
      case None => throw new RuntimeException(s"No value set for property [$key]")
    }
  }

  val route: Route = pathPrefix("api" / AGU.APIVERSION) {
    siteServices ~ userRoute ~ keyPairRoute ~ catalogRoutes ~ appSettingServiceRoutes ~
      apmServiceRoutes ~ nodeRoutes ~ appsettingRoutes ~ discoveryRoutes ~ siteServiceRoutes ~ commandRoutes ~
      esServiceRoutes ~ workflowRoutes ~ adminRoute
  }

  AGU.startUp(List("2551","2552"))

  val bindingFuture = Http().bindAndHandle(route, AGU.HOST, AGU.PORT)
  val keyFilesDir: String = s"${Constants.tempDirectoryLocation}${Constants.FILE_SEPARATOR}"

  // scalastyle:off cyclomatic.complexity
  def appSettingServiceRoutes: Route = post {
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
  } ~ path("apm" / LongNumber) {
    siteId => get {
      val serverDetails = Future {
        Site.fromNeo4jGraph(siteId).map { site =>
          val aPMServerDetails = getAPMServers
          logger.info(s"All Sever details : $aPMServerDetails")
          val list = aPMServerDetails.filter(server => {
            server.monitoredSite.exists(monitoredSite => monitoredSite.id == site.id)
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

                val keyPairInfo = KeyPairInfo(keyName, Some(keyMaterial), Some(filePath), UploadedKeyPair)
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

  def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    User.fromNeo4jGraph(userId).flatMap { user =>
      user.publicKeys.find { keyPair =>
        keyPair.id.contains(keyId)
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
                if (name.equalsIgnoreCase("userName") || name.equalsIgnoreCase("passPhase")) {
                  accum + ((name, value))
                }
                else {
                  accum
                }
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

  def getOrCreateKeyPair(keyName: String, keyMaterial: String, keyFilePath: Option[String], status: KeyPairStatus, defaultUser: Option[String],
                         passPhase: Option[String]): KeyPairInfo = {
    val mayBeKeyPair = Neo4jRepository.getSingleNodeByLabelAndProperty("KeyPairInfo", "keyName", keyName).flatMap(
      node => KeyPairInfo.fromNeo4jGraph(node.getId))

    mayBeKeyPair match {
      case Some(keyPairInfo) =>
        KeyPairInfo(keyPairInfo.id, keyName, keyPairInfo.keyFingerprint, Some(keyMaterial), if (keyFilePath.isEmpty) keyPairInfo.filePath else keyFilePath,
          status, if (defaultUser.isEmpty) keyPairInfo.defaultUser else defaultUser, if (passPhase.isEmpty) keyPairInfo.passPhrase else passPhase)
      case None => KeyPairInfo(keyName, Some(keyMaterial), keyFilePath, status)
    }
  }

  def saveKeyPair(keyPairInfo: KeyPairInfo): Option[KeyPairInfo] = {
    val filePath = getKeyFilePath(keyPairInfo.keyName)
    try {
      FileUtils.createDirectories(keyFilesDir)
      FileUtils.saveContentToFile(filePath, keyPairInfo.keyMaterial.get)
      // TODO: change permissions to 600
    } catch {
      case e: Throwable => logger.error(e.getMessage, e)
    }
    val node = keyPairInfo.toNeo4jGraph(KeyPairInfo(keyPairInfo.id, keyPairInfo.keyName,
      keyPairInfo.keyFingerprint, keyPairInfo.keyMaterial, Some(filePath),
      keyPairInfo.status, keyPairInfo.defaultUser, keyPairInfo.passPhrase))
    KeyPairInfo.fromNeo4jGraph(node.getId)
  }

  def getKeyFilePath(keyName: String): String = s"$keyFilesDir$keyName.pem"

  def catalogRoutes: Route = pathPrefix("catalog") {
    path("images" / "view") {
      get {
        val images: Future[Page[ImageInfo]] = Future {
          val imageLabel: String = "ImageInfo"
          val nodesList = Neo4jRepository.getNodesByLabel(imageLabel)
          val imageInfoList = nodesList.flatMap(node => ImageInfo.fromNeo4jGraph(node.getId))

          Page[ImageInfo](imageInfoList)
        }

        onComplete(images) {
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
        val softwares = Future {
          val softwareLabel: String = "SoftwaresTest2"
          val nodesList = Neo4jRepository.getNodesByLabel(softwareLabel)
          val softwaresList = nodesList.flatMap(node => Software.fromNeo4jGraph(node.getId))
          Page[Software](softwaresList)
        }
        onComplete(softwares) {
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
                  val memInfo = instance.memoryInfo.map(_.total).getOrElse(0.0)
                  val dskInfo = instance.rootDiskInfo.map(_.total).getOrElse(0.0)
                  InstanceFlavor(instance.instanceType.getOrElse(""), None, memInfo, dskInfo)
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

  def nodeRoutes: Route = pathPrefix("node") {
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
            case Some(node) => Instance.fromNeo4jGraph(node.getId)
            case None =>
              val name = "echo node"
              val tags: List[KeyValueInfo] = List(KeyValueInfo(None, "tag", "tag"))
              val processInfo = ProcessInfo(1, 1, "init")
              Option(Instance(name, tags, Set(processInfo)))
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

  def siteServiceRoutes: Route = pathPrefix("sites") {
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
                Some(Site1(site.id, site.siteName, filteredInstances, site.reservedInstanceDetails, site.filters, site.loadBalancers, site.scalingGroups,
                  site.groupsList, List.empty[Application], site.groupBy, site.scalingPolicies))
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
                val siteToSave = Site1(site.id, site.siteName, newListOfInstances, site.reservedInstanceDetails,
                  site.filters, site.loadBalancers, site.scalingGroups, site.groupsList,
                  List.empty[Application], site.groupBy, site.scalingPolicies)
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
          val mayBeSite = Site1.fromNeo4jGraph(siteId)
          val siteDelta = mayBeSite match {
          case Some(site) =>
            val instancesBeforeSync = site.instances
            val synchedSite = populateInstances(site)
            synchedSite.toNeo4jGraph(synchedSite)
            val futureSaveSite = saveCachedSite(siteId)
            futureSaveSite.onComplete {
              case Success(saved) => saved match {
                case true => logger.info("saved site successfully from distributed cache")
                case false => logger.warn("there is no site with given id in distributed cache to be saved")
              }
              case Failure(ex) => logger.error(s"Unable to get Site from distributed cache. Failed with : ${ex.getMessage}", ex)
            }
            val filteredSite = populateFilteredInstances(siteId, List.empty[Filter])
            filteredSite.map {
              case Some(siteInCache) =>
                val instancesAfterSync = siteInCache.instances
                val beforeSyncInstanceIds = instancesBeforeSync.flatMap(instance => instance.instanceId).toSet
                val afterSyncInstanceIds = instancesAfterSync.flatMap(instance => instance.instanceId).toSet
                val commonInstanceIds = beforeSyncInstanceIds.intersect(afterSyncInstanceIds)
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
                  SiteDeltaStatus.toDeltaStatus("CHANGED")
                } else {
                  SiteDeltaStatus.toDeltaStatus("UNCHANGED")
                }
                Some(SiteDelta(site.id, deltaStatus, addedInstances, deletedInstances))
              case None =>
                logger.warn(s"could not get site from cache for siteId : $siteId")
                None
            }
          case None =>
            logger.warn(s"could not get site with siteId : $siteId")
            Future.successful(None)
        }
        onComplete(siteDelta) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to get Site Delta. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Site Delta.")
        }
      }
    } ~ path(LongNumber / "instances" / Segment / "users") { (siteId, id) =>
      put {
        entity(as[InstanceUser]) { user =>
          val addInstanceUser = Future {
            val mayBeInstance = getInstance(siteId, id)
            mayBeInstance.foreach { instance =>
              val listOfExistingUsers = instance.existingUsers
              val userInList = listOfExistingUsers.find(instanceUser => instanceUser.userName.equals(user.userName))
              userInList match {
                case Some(oldUser) =>
                  val newUser = oldUser.copy(publicKeys = user.publicKeys ::: oldUser.publicKeys)
                  newUser.toNeo4jGraph(newUser)
                case None =>
                  val newListOfExistingUsers = user :: listOfExistingUsers
                  val newInstance = instance.copy(existingUsers = newListOfExistingUsers)
                  newInstance.toNeo4jGraph(newInstance)
              }
            }
            "InstanceUser added/updated successfully"
          }
          onComplete(addInstanceUser) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to add InstanceUser. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to add Instance User.")
          }
        }
      }
    } ~ path(LongNumber / "instances" / Segment / "users") { (siteId, id) =>
      get {
        val instanceUsers = Future {
          val mayBeInstance = getInstance(siteId, id)
          mayBeInstance.map { instance =>
            instance.existingUsers
          }
        }
        onComplete(instanceUsers) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to get InstanceUsers List. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to add Instance Users List.")
        }
      }
    } ~ path(LongNumber / "instances" / Segment / "users") { (siteId, id) =>
      put {
        entity(as[Map[String, String]]) { userDetail =>
          val associateUser = Future {
            val mayBeInstance = getInstance(siteId, id)
            if (userDetail.contains("userName")) {
              val sshAccessInfo = mayBeInstance.flatMap(instance => instance.sshAccessInfo)
              sshAccessInfo.foreach { ssh =>
                val newSSHAccessInfo = ssh.copy(userName = userDetail.get("userName"))
                newSSHAccessInfo.toNeo4jGraph(newSSHAccessInfo)
              }
            }
            "User associated to Instance successfully"
          }
          onComplete(associateUser) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to associate User to Instance. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to associate User To Instance.")
          }
        }
      }
    } ~ path(LongNumber / "instances" / Segment) { (siteId, name) =>
      put {
        entity(as[List[String]]) { instanceIds =>
          val associateUsers = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite.foreach { site =>
              val instances = site.instances
              instanceIds.foreach { id =>
                val mayBeInstance = instances.find(instance => instance.instanceId.contains(id))
                val sshAccessInfo = mayBeInstance.flatMap(instance => instance.sshAccessInfo)
                sshAccessInfo.foreach { ssh =>
                  val newSSHAccessInfo = ssh.copy(userName = Some(name))
                  newSSHAccessInfo.toNeo4jGraph(newSSHAccessInfo)
                }
              }
            }
            "Users associated to Instance successfully"
          }
          onComplete(associateUsers) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to associate Users to Instance. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to associate Users To Instance.")
          }

        }
      }
    } ~ path(LongNumber / "instances" / Segment / "status") { (siteId, ids) =>
      get {
        val instanceStatuses = Future {
          val idList = ids.split(",").toList
          val mayBeSite = Site1.fromNeo4jGraph(siteId)
          val siteFilters = mayBeSite.map(site => site.filters).getOrElse(List.empty[SiteFilter])
          val accountInfo = siteFilters.headOption.map(clue => clue.accountInfo)
          accountInfo.map { info =>
            val regionVsInstanceIds = getRegionVsInstanceIds(siteId, idList)
            regionVsInstanceIds.keySet.foldLeft(Map.empty[String, String]) { (map, region) =>
              val provider = info.providerType
              if (provider == InstanceProvider.toInstanceProvider("AWS")) {
                val amazonEC2 = AWSComputeAPI.getComputeAPI(info, region)
                map ++ AWSComputeAPI.getInstanceStatuses(amazonEC2, idList)
              } else {
                map
              }
            }
          }
        }
        onComplete(instanceStatuses) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to get Instance Statuses. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Instance Statuses.")
        }
      }
    } ~ path(LongNumber / "groupby") { siteId =>
      put {
        entity(as[String]) { groupBy =>
          val response = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) => val siteToSave = site.copy(groupBy = groupBy)
                siteToSave.toNeo4jGraph(siteToSave)
                "Added GroupBy successfully!"
              case None => throw new NotFoundException(s"Site Entity not found with ID : $siteId")
            }
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
            case Failure(exception) =>
              logger.error(s"Unable to add GroupBy ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to add groupBy")
          }
        }
      }
    } ~ path(LongNumber / "groupby") { siteId =>
      get {
        val siteGroupBy = Future {
          val mayBeSite = Site1.fromNeo4jGraph(siteId)
          val mayBeGroupBy = mayBeSite match {
            case Some(site) => site.groupBy
            case None => throw new NotFoundException(s"Site Entity with ID : $siteId Not Found")
          }
          mayBeGroupBy
        }

        onComplete(siteGroupBy) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to get Site GroupBy. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to get Site GroupBy.")
        }
      }
    } ~ path(LongNumber / "instances" / Segment) {
      (siteId, instanceId) =>
        delete {
          val response = Future {
            val mayBeSite = Site1.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) =>
                val mayBeInstance = site.instances.find(instance => instance.instanceId.contains(instanceId))
                mayBeInstance match {
                  case Some(instance) =>
                    instance.id match {
                      case Some(instId) =>
                        site.groupsList.foreach { group =>
                          val mayBeInstanceGroup = group.instances.find(instanceInGroup => instanceInGroup.instanceId.contains(instanceId))
                          mayBeInstanceGroup match {
                            case Some(instanceGroup) =>
                              instanceGroup.id.foreach { instanceGroupId =>
                                Neo4jRepository.deleteRelationship(instanceGroupId, instId, "HAS_Instance")
                              }
                            case None =>
                              logger.warn(s"InstanceGroup is not found with instanceId $instanceId")
                          }
                        }
                        Neo4jRepository.deleteRelationship(siteId, instId, "HAS_Instance")
                        "Deleted successfully!"
                      case None =>
                        logger.warn("Instance node id is None")
                        "Unable to delete the instance!"
                    }
                  case None => throw new NotFoundException(s"Instance Entity with id : $instanceId is not found")
                }
              case None => throw new NotFoundException(s"Site Entity with id : $siteId is not found")
            }
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, "Deleted Successfully!")
            case Failure(exception) =>
              logger.error(s"Unable to delete the Instance with id $instanceId ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to delete the Instance with id $instanceId")
          }
        }
    } ~ path(LongNumber / "instances" / Segment / "tags") {
      (siteId, instanceId) => put {
        entity(as[List[KeyValueInfo]]) {
          tagsToAdd =>
            val response = Future {
              addTags(siteId, instanceId, tagsToAdd)
              "Updated successfully!"
            }
            onComplete(response) {
              case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
              case Failure(exception) =>
                logger.error(s"Unable to update the tags ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to update the tags!")
            }
        }
      }
    } ~ path(LongNumber / "instances" / "tags") {
      siteId => put {
        entity(as[Map[String, JsValue]]) {
          map =>
            val response = Future {
              if (map.contains("instanceIds")) {
                val instanceIds = map("instanceIds").asInstanceOf[List[String]]
                val tags = map("tags").asInstanceOf[List[KeyValueInfo]]
                instanceIds.foreach(instanceId => addTags(siteId, instanceId, tags))
                "Updated successfully"
              } else {
                throw new Exception("Unable get istanceIds from request")
              }
            }
            onComplete(response) {
              case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
              case Failure(exception) =>
                logger.error(s"Unable to update the tags ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to update tags for instances")
            }
        }
      }
    } ~ path(LongNumber / "applications") {
      siteId => put {
        entity(as[Application]) {
          application =>
            val response = Future {
              val mayBeSite = Site1.fromNeo4jGraph(siteId)
              mayBeSite match {
                case Some(site) => saveApplication(site, application)
                  "Inserted Application successfully!"
                case None => throw new NotFoundException(s"Site Entity not found with ID : $siteId")
              }
            }
            onComplete(response) {
              case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
              case Failure(exception) =>
                logger.error(s"Unabel to save the Application ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, "Unable to save the application")
            }
        }
      }
    } ~ path(LongNumber / "applications") { siteId =>
      get {
        val applicationsList = Future {
          val mayBeSite = Site1.fromNeo4jGraph(siteId)
          mayBeSite match {
            case Some(site) =>
              val applicationsList = site.applications
              Page[Application](applicationsList)
            case None =>
              logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
              Page[Application](List.empty[Application])
          }
        }
        onComplete(applicationsList) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(ex) =>
            logger.error(s"Unable to get List; Failed with ${ex.getMessage}", ex)
            complete(StatusCodes.BadRequest, "Unable to get List of Applications")
        }
      }
    } ~ path(LongNumber / "applications" / LongNumber) {
      (siteId, appId) =>
        get {
          val response = Future {
            Application.fromNeo4jGraph(appId)
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
            case Failure(exception) =>
              logger.error(s"Unable to get Application entity ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to get application with id $appId")
          }
        }
    } ~ path(LongNumber / "applications" / LongNumber) {
      (siteId, appId) =>
        delete {
          val response = Future {
            Neo4jRepository.deleteEntity(appId)
            "Deleted Successfully!"
          }
          onComplete(response) {
            case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
            case Failure(exception) =>
              logger.error(s"Unable to delete the Application with ID : $appId ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to delete the Application with ID : $appId")
          }
        }
    } ~ path(LongNumber / "applications") {
      (siteId) =>
        post {
          entity(as[Application]) {
            application =>
              val response = Future {
                if (application.id.nonEmpty) {
                  application.toNeo4jGraph(application)
                  "Updated successfully!"
                } else {
                  throw new Exception("Entity ID is mandatory for updating")
                }
              }
              onComplete(response) {
                case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
                case Failure(exception) =>
                  logger.error(s"Unable to update the Applicaation ${exception.getMessage}", exception)
                  complete(StatusCodes.BadRequest, s"Unable to update Application with id ${application.id}")
              }
          }
        }
    } ~ path(LongNumber / "connections") { siteId =>
      get {
        parameter('strategy) { strategy => {
          //Collect all connection based among instance based on the strategy
          val connections = Future {
            def strategyConnection(getConnections: Instance => List[InstanceConnection]) = {
              val siteOpt = Site1.fromNeo4jGraph(siteId)
              val site = siteOpt.map(site =>
                site.instances.flatMap(instance => getConnections(instance))
              ).getOrElse(List.empty[InstanceConnection])
              site
            }
            ConnectionStrategy.toStrategy(strategy) match {
              case ConnectionStrategy.Ssh => strategyConnection(instance => instance.liveConnections)
              case ConnectionStrategy.SecurityGroup => strategyConnection(instance => instance.estimatedConnections)
            }
          }
          onComplete(connections) {
            case Success(connectionResponse) =>
              if (connectionResponse.isEmpty) {
                complete(StatusCodes.NoContent)
              } else {
                complete(StatusCodes.OK, connectionResponse)
              }
            case Failure(exception) =>
              logger.error(s"Unable to get connection for site id $siteId. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to get Site connection.")
          }
        }
        }
      }
    } ~ path(LongNumber / "instances" / Segment / Segment) { (siteId, ids, action) =>
      get {
        onComplete(instancesAction(siteId, ids, action)) {
          case Success(instancesResponse) =>
            if (instancesResponse.isEmpty) {
              complete(StatusCodes.BadRequest, "Unable to apply action on the instances.")
            } else {
              complete(StatusCodes.OK, instancesResponse)
            }
          case Failure(exception) =>
            logger.error(s"Unable to apply action in instance. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to apply action on the instances.")
        }
      }
    } ~
      path("site" / LongNumber / "policies") {
        (siteId) => {
          get {
            val mayBePolicy = Future {
              SiteManagerImpl.getAutoScalingPolicies(siteId)
            }
            onComplete(mayBePolicy) {
              case Success(policyList) => complete(StatusCodes.OK, policyList)
              case Failure(ex) => logger.info(s"Error while retrieving policies of $siteId  details", ex)
                complete(StatusCodes.BadRequest, s"Error while retrieving policies of  ${siteId} details")
            }
          }
          post {
            entity(as[AutoScalingPolicy]) {
              policy =>
                val mayBeAdded = Future {
                  SiteManagerImpl.addAutoScalingPolicy(siteId, policy)
                }
                onComplete(mayBeAdded) {
                  case Success(policy) => complete(StatusCodes.OK, "Policy added successfully")
                  case Failure(ex) => logger.info(s"Error while retrieving policies of $siteId  details", ex)
                    complete(StatusCodes.BadRequest, s"Error while retrieving policies of  ${siteId} details")
                }
            }
          }
        }

      } ~
      path("site" / LongNumber / "policies" / Segment) {
        (siteId, policyId) => {
          get {
            val mayBePolicy =
              Future {
                SiteManagerImpl.getAutoScalingPolicy(siteId, policyId)
              }
            onComplete(mayBePolicy) {
              case Success(policyList) => complete(StatusCodes.OK, policyList)
              case Failure(ex) => logger.info(s"Error while retrieving policy $siteId and $policyId ", ex)
                complete(StatusCodes.BadRequest, s"Error while retrieving policiy $siteId and $policyId")
            }
          }
          delete {
            val mayBeDeleted = Future {
              SiteManagerImpl.deletePolicy(policyId)
            }
            onComplete(mayBeDeleted) {
              case Success(deleted) => complete(StatusCodes.OK, "Policy $policyId deleted successfully")
              case Failure(ex) => logger.info(s"Error in deleting policy $policyId ", ex)
                complete(StatusCodes.BadRequest, s"Error while deleting policy $policyId")
            }
          }
        }
      }

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
          Set.empty[ProcessInfo], instance.image, List.empty[InstanceUser], instance.account, instance.availabilityZone, instance.privateDnsName,
          instance.privateIpAddress, None, instance.elasticIP, instance.monitoring, instance.rootDeviceType, List.empty[InstanceBlockDeviceMappingInfo],
          instance.securityGroups, instance.reservedInstance, instance.region)
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
          instance.processes, instance.image, List.empty[InstanceUser], instance.account, instance.availabilityZone, instance.privateDnsName,
          instance.privateIpAddress, None, instance.elasticIP, instance.monitoring, instance.rootDeviceType, List.empty[InstanceBlockDeviceMappingInfo],
          instance.securityGroups, instance.reservedInstance, instance.region)
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
          instance.processes, instance.image, List.empty[InstanceUser], instance.account, instance.availabilityZone, instance.privateDnsName,
          instance.privateIpAddress, None, instance.elasticIP, instance.monitoring, instance.rootDeviceType, instance.blockDeviceMappings,
          instance.securityGroups, instance.reservedInstance, instance.region)
    }
  }

  def populateFilteredInstances(siteId: Long, filters: List[Filter]): Future[Option[Site1]] = {
    val futureSite = SharedSiteCache.getSite(siteId.toString)
    futureSite.map {
      case Some(site) =>
        val instances = site.instances
        val (siteFiltersToSave, filteredInstances) = if (filters.nonEmpty) {
          val siteFilters = site.filters
          val listOfSiteFilters = siteFilters.map {
            siteFilter => SiteFilter(siteFilter.id, siteFilter.accountInfo, filters)
          }
          val listOfInstances = instances.flatMap {
            instance =>
              val filterExists = filters.find(filter => filterExistsInTags(instance, filter))
              filterExists.map(filter => instance)
          }
          (listOfSiteFilters, listOfInstances)
        }
        else {
          (site.filters, instances)
        }
        val instancesToSave = getInstancesToSave(filteredInstances.toSet)
        val lbsToSave = getLoadBalancersToSave(site.loadBalancers)
        val sgsToSave = getScalingGroupsToSave(site.scalingGroups)
        val rInstancesToSave = getReservedInstancesToSave(site.reservedInstanceDetails)
        val siteToSave = Site1(site.id, site.siteName, instancesToSave, rInstancesToSave, siteFiltersToSave, lbsToSave, sgsToSave, site.groupsList,
          List.empty[Application], site.groupBy, site.scalingPolicies)
        siteToSave.toNeo4jGraph(siteToSave)
        SharedSiteCache.putSite(siteId.toString, siteToSave)
        Some(siteToSave)
      case None => logger.warn(s"could not get Site from cache for siteId : $siteId")
        None
    }
  }

  logger.info(s"Server online at http://${AGU.HOST}:${AGU.PORT}")

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
          e.copy(state = instance.state, publicDnsName = instance.publicDnsName, privateDnsName = instance.privateDnsName,
            privateIpAddress = instance.privateIpAddress, publicIpAddress = instance.publicIpAddress, reservedInstance = instance.reservedInstance)

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
          s.copy(launchConfigurationName = sg.launchConfigurationName, status = sg.status, instanceIds = sg.instanceIds)
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

  def addTags(siteId: Long, instanceId: String, tagsToAdd: List[KeyValueInfo]): Unit = {
    val mayBeSite = Site1.fromNeo4jGraph(siteId)
    mayBeSite.foreach {
      site =>
        val accountInfo = site.filters.map { siteFilter =>
          siteFilter.accountInfo
        }
        val instance = site.instances.find(instance => instance.instanceId.contains(instanceId))
        instance.foreach { inst =>
          val instToSave = inst.copy(tags = tagsToAdd)
          instToSave.toNeo4jGraph(instToSave)
          EsManager.indexEntity[Instance](inst, site.siteName, "instance")
        }
    }
  }

  def saveApplication(site: Site1, application: Application): Unit = {
    val applications = site.applications
    val mayBeApp = applications.find(app => application.name.exists(name => app.name.contains(name)))
    val appToSave = mayBeApp match {
      case Some(app) =>
        val app1 = app.copy(instances = application.instances)
        app1.toNeo4jGraph(app1)
      case None => application.toNeo4jGraph(application)
    }
  }

  def populateInstances(site: Site1): Site1 = {
    val siteFilters = site.filters
    val siteNode = site.toNeo4jGraph(site)
    logger.info(s"Parsing instance : ${site.instances}")
    val (instances, reservedInstanceDetails, loadBalancer, scalingGroup) = siteFilters.foldLeft((List[Instance](), List[ReservedInstanceDetails](),
      List.empty[LoadBalancer], List.empty[ScalingGroup])) {
      (result, siteFilter) =>
        val accountInfo = siteFilter.accountInfo
        val mayBeRegion = accountInfo.regionName
        val (instancesList, reservedInstanceDetailsList, loadBalancerList, scalingGroupList) = result
        mayBeRegion.map { regionName =>
          val amazonEC2 = AWSComputeAPI.getComputeAPI(accountInfo, regionName)
          val instances = AWSComputeAPI.getInstances(amazonEC2, accountInfo)
          val reservedInstanceDetails = AWSComputeAPI.getReservedInstances(amazonEC2)
          val loadBalancers = AWSComputeAPI.getLoadBalancers(accountInfo)
          val scalingGroup = AWSComputeAPI.getAutoScalingGroups(accountInfo)
          (instancesList ::: instances, reservedInstanceDetailsList ::: reservedInstanceDetails, loadBalancerList ::: loadBalancers, scalingGroupList :::
            scalingGroup)
        }.getOrElse(result)
    }
    val site1 = Site1(Some(siteNode.getId), site.siteName, instances, reservedInstanceDetails, site.filters, loadBalancer, scalingGroup, List(),
      List.empty[Application], site.groupBy, site.scalingPolicies)
    site1
  }

  def saveCachedSite(siteId: Long): Future[Boolean] = {
    val futureSite = SharedSiteCache.getSite(siteId.toString)
    futureSite.map { mayBeSite =>
      mayBeSite.foreach { site =>
        site.toNeo4jGraph(site)
        SharedSiteCache.removeSite(siteId.toString)
      }
      mayBeSite.isDefined
    }
  }

  def getInstance(siteId: Long, id: String): Option[Instance] = {
    val mayBeSite = Site1.fromNeo4jGraph(siteId)
    mayBeSite.flatMap { site =>
      val instances = site.instances
      instances.find(instance => instance.instanceId.contains(id))
    }
  }

  def getRegionVsInstanceIds(siteId: Long, instanceIds: List[String]): Map[String, List[String]] = {
    instanceIds.foldLeft(Map.empty[String, List[String]]) { (regionsVsInstanceIds, id) =>
      val mayBeInstance = getInstance(siteId, id)
      val mayBeRegion = mayBeInstance.flatMap(i => i.region)
      mayBeRegion.map { region =>
        val mayBeList = regionsVsInstanceIds.get(region)
        mayBeList match {
          case Some(list) => regionsVsInstanceIds + ((region, id :: list))
          case None => regionsVsInstanceIds + ((region, List(id)))
        }
      }.getOrElse(regionsVsInstanceIds)
    }
  }

  private def instancesAction(siteId: Long, ids: String, action: String) =
    Future {
      val idList = ids.split(",").toList
      val siteOpt = Site1.fromNeo4jGraph(siteId)
      val accountInfoOpt = siteOpt
        .flatMap(site => site.filters.headOption)
        .map(siteFilter => siteFilter.accountInfo)

      val regionVsInstance =
        siteOpt.map(site => {
          val instances = site.instances.filter(instance => idList.contains(instance.instanceId.getOrElse("")))
          instances
        }).getOrElse(List.empty[Instance]).groupBy(_.region.getOrElse(""))

      accountInfoOpt.map(accountInfo =>
        regionVsInstance.flatMap { case (region, instances) => executeAction(action, region, instances, accountInfo)
        }).getOrElse(Map.empty[String, String])
    }

  private def executeAction(action: String, region: String, instances: List[Instance], accountInfo: AccountInfo) = {
    val amazonEC2 = AWSComputeAPI.getComputeAPI(accountInfo, region)
    val instanceIds = instances.flatMap(instance => instance.instanceId)
    InstanceActionType.toType(action) match {
      case InstanceActionType.Start => AWSComputeAPI.startInstance(amazonEC2, instanceIds)
      case InstanceActionType.Stop => AWSComputeAPI.stopInstance(amazonEC2, instanceIds)
      case InstanceActionType.CreateSnapshot => {
        val snapShots =
          for {instance <- instances
               instanceBlockingDevice <- instance.blockDeviceMappings
               volumeId <- instanceBlockingDevice.volume.volumeId
          } yield {
            val snapShots = AWSComputeAPI.createSnapshot(amazonEC2, volumeId)
            val volumeNew = instanceBlockingDevice.volume.copy(
              currentSnapshot = Some(snapShots),
              snapshotCount = instanceBlockingDevice.volume.snapshotCount.map(count => count + 1))
            val node = volumeNew.toNeo4jGraph(volumeNew)
            (volumeId -> node.getId.toString)
          }
        snapShots.toMap
      }
      case InstanceActionType.CreateImage => {
        val opt =
          for {instance <- instances.headOption
               image <- instance.image
               imageName <- image.name
               instanceId <- instance.instanceId
          } yield (Map(instanceId -> AWSComputeAPI.createImage(amazonEC2, imageName, instanceId)))
        opt.getOrElse(Map.empty[String, String])
      }
      case _ => throw new Exception("Action not found")
    }
  }

  def indexSite(site: Site1): Unit = {
    val index = site.siteName
    site.instances.foreach(instance => EsManager.indexEntity[Instance](instance, index, "instance"))
    site.loadBalancers.foreach(loadBalancer => EsManager.indexEntity[LoadBalancer](loadBalancer, index, "lb"))
  }

  // scalastyle:off method.length cyclomatic.complexity
  def siteServices: Route = pathPrefix("site") {
    get {
      parameters('viewLevel.as[String]) {
        (viewLevel) =>
          val result = Future {
            logger.info("View level is..." + viewLevel)
            //Instead of applying map and flat operations at distinct  places,flatMap used directly though container holds only List[Nodes]
            //Use of map here produces results like List[Option[Site]] but List[Site1] would be better option for next operations.
            Neo4jRepository.getNodesByLabel("Site1").flatMap { siteNode =>
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
        case Success(isDeleted) =>
          if (isDeleted) {
            complete(StatusCodes.OK, "Site deleted successfully")
          }
          else {
            complete(StatusCodes.OK, "Error in site deletion")
          }
        case Failure(ex) => logger.info("Failed to delete entity", ex)
          complete(StatusCodes.BadRequest, "Deletion failed")
      }
    }
  } ~ path("site" / LongNumber / "instances" / Segment) {
    (siteId, instanceId) => {
      delete {
        val mayBeDelete = Future {
          SiteManagerImpl.deleteIntanceFromSite(siteId, instanceId)
        }
        onComplete(mayBeDelete) {
          case Success(isDeleted) =>
            if (isDeleted) {
              complete(StatusCodes.OK, "Instance deleted successfully")
            }
            else {
              complete(StatusCodes.OK, "Error in Instance deletion")
            }
          case Failure(ex) => logger.error("Failed to delete the insatnce", ex)
            complete(StatusCodes.BadRequest, "Failed to delete instance")
        }
      }
    }
  } ~
    path("site" / LongNumber / "policies" / Segment) {
      (siteId, policyId) => {
        delete {
          val maybeDelete = Future {
            SiteManagerImpl.deletePolicy(policyId)
          }
          onComplete(maybeDelete) {
            case Success(isDeleted) =>
              if (isDeleted) {
                complete(StatusCodes.OK, "Policy deleted successfully")
              }
              else {
                complete(StatusCodes.OK, "Error in policy deletion")
              }
            case Failure(ex) => logger.info(s"Error while deleting policy $policyId", ex)
              complete(StatusCodes.BadRequest, "Error while deleting policy")
          }
        }
      }
    } ~
    path("site" / LongNumber / "policies" / Segment) {
      (siteId, policyId) => {
        get {
          val mayBePolicy = Future {
            SiteManagerImpl.getAutoScalingPolicy(siteId, policyId)
          }
          onComplete(mayBePolicy) {
            case Success(somePolicy) =>
              somePolicy match {
                case Some(policy) => complete(StatusCodes.OK, policy)
                case None => complete(StatusCodes.OK, s"Policy $policyId is not available")
              }
            case Failure(ex) => logger.info(s"Error while retrieving policy $policyId details", ex)
              complete(StatusCodes.BadRequest, s"Error while retrieving policy  ${policyId} details")
          }
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

  implicit object ContextTypeFormat extends RootJsonFormat[ContextType] {

    override def write(obj: ContextType): JsValue = {
      JsString(obj.contextType.toString)
    }

    override def read(json: JsValue): ContextType = {
      json match {
        case JsString(str) => ContextType.toContextType(str)
        case _ => throw DeserializationException("Unable to deserialize Context Type")
      }
    }
  }

  implicit object commandExecutionContextFormat extends RootJsonFormat[CommandExecutionContext] {
    val fieldNames = List("contextName", "contextType", "contextObject", "parentContext", "instances", "siteId", "user")

    override def write(i: CommandExecutionContext): JsValue = {
      val contextObject = i.contextObject match {
        case Some(ctx) if ctx.isInstanceOf[Site1] =>
          AGU.objectToJsValue[Site1](fieldNames(3), Some(i.contextObject.asInstanceOf[Site1]), site1Format)
        case Some(ctx) if ctx.isInstanceOf[Instance] =>
          AGU.objectToJsValue[Instance](fieldNames(3), Some(i.contextObject.asInstanceOf[Instance]), instanceFormat)
        case _ =>
          logger.warn("Unkown type found other than Site and Instance")
          throw new Exception("Unkown type found other than Site and Instance. Base entity should have Site or instance types")
      }
      // scalastyle:off magic.number
      val fields = List((fieldNames(0), JsString(i.contextName))) ++
        List((fieldNames(1), ContextTypeFormat.write(i.contextType))) ++
        contextObject ++
        AGU.objectToJsValue[CommandExecutionContext](fieldNames(3), i.parentContext, commandExecutionContextFormat) ++
        i.instances.map(instance => (fieldNames(4), JsString(instance))) ++
        List((fieldNames(5), JsNumber(i.siteId))) ++
        AGU.stringToJsField(fieldNames(6), i.user)
      // scalastyle:on magic.number
      JsObject(fields: _*)
    }

    override def read(json: JsValue): CommandExecutionContext = {
      json match {
        case JsObject(map) =>
          val mayBeCec = for {
            contextName <- AGU.getProperty[String](map, fieldNames(0))
            contextType <- AGU.getProperty[String](map, fieldNames(1))
            instances <- AGU.getProperty[Array[String]](map, fieldNames(4))
            siteId <- AGU.getProperty[Long](map, fieldNames(5))
          } yield {
            val contextObj = if (map.contains("contextObject")) {
              val contextObj = map("contextObject").asJsObject
              if (contextObj.fields.contains("instanceId")) {
                instanceFormat.read(contextObj)
              } else {
                site1Format.read(contextObj)
              }
            } else {
              throw DeserializationException("Unable to parse the contextObject")
            }

            CommandExecutionContext(
              contextName,
              ContextType.toContextType(contextType),
              Some(contextObj),
              AGU.getProperty[String](map, fieldNames(3)).map(parentContext => commandExecutionContextFormat.read(parentContext.asInstanceOf[JsValue])),
              instances,
              siteId,
              AGU.getProperty[String](map, fieldNames(6))
            )
          }
          mayBeCec match {
            case Some(commandExecutionContext) => commandExecutionContext
            case None => logger.warn("Unable to deserialize the CommandExecutionContext, Unknown properties found")
              throw DeserializationException("Unable to deserialize the CommandExecutionContext, Unknown properties found")
          }
        case _ =>
          throw DeserializationException("Unable deseriailize the CommandExecutionContext")
      }
    }
  }

  implicit object LineTypeFormat extends RootJsonFormat[LineType] {

    override def write(obj: LineType): JsValue = {
      JsString(obj.lineType.toString)
    }

    override def read(json: JsValue): LineType = {
      json match {
        case JsString(str) => LineType.toLineType(str)
        case _ => throw DeserializationException("Unable to deserialize Line Type")
      }
    }
  }

  implicit val lineFormat = jsonFormat(Line.apply, "values", "lineType", "hostIdentifier")
  implicit val commandResultFormat = jsonFormat(CommandResult.apply, "result", "currentContext")

  def commandRoutes: Route = pathPrefix("terminal") {
    path("start") {
      put {
        entity(as[CommandExecutionContext]) { context =>
          val session = Future {
            val terminalId = generateRandomId
            val date = new Date
            val instanceIds = context.instances
            val (_, sshSessions) = instanceIds.foldLeft(None: Option[String], List.empty[SSHSession]) { (result, instanceId) =>
              val (hostPropToUse, listOfSSHSessions) = result
              val mayBeInstance = getInstance(context.siteId, instanceId)
              val mayBeHostPropAndSession = mayBeInstance.map { instance =>
                buildSSHSession(instance, hostPropToUse)
              }
              mayBeHostPropAndSession match {
                case Some(hostAndSession) =>
                  val (host, sshSession) = hostAndSession
                  (Some(host), sshSession :: listOfSSHSessions)
                case None => result
              }
            }
            val session = TerminalSession(terminalId, None, List.empty[String], context, sshSessions, date, date)
            SharedSessionCache.putSession(session.id.toString, session)
            s"Session created with session id: ${session.id}"
          }
          onComplete(session) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to start Terminal Session. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to Start Terminal Session.")
          }
        }
      }
    } ~ path(LongNumber / "execute") { terminalId =>
      put {
        entity(as[String]) { commandLine =>
            val futureOfSession = SharedSessionCache.getSession(terminalId.toString)
            val commandResult = futureOfSession.map { mayBeSession =>
              val currentCmdExecContext = mayBeSession.map(_.currentCmdExecContext)
              val result = mayBeSession match {
                case Some(terminalSession) =>
                  val newSession = terminalSession.copy(lastUsedAt = new Date)
                  if (commandLine.indexOf('|') != -1) {
                    val executedCommandResult = executePipedCommand(newSession, commandLine)
                    Some(executedCommandResult)
                  } else if (terminalSession.currentCmdExecContext.instances.length > 0) {
                    val executedCommandResult = executeSSHCommand(newSession, commandLine)
                    Some(executedCommandResult)
                  } else {
                    logger.warn(s"Not a valid command; commandLine : $commandLine")
                    None
                  }
                case None =>
                  // first thing that will hit us when we start running on a cluster
                  throw new IllegalStateException("no session with the given terminal id")
              }
              result.getOrElse(CommandResult(List.empty[Line], currentCmdExecContext))
            }
          onComplete(commandResult) {
            case Success(responseMessage) => complete(StatusCodes.OK, responseMessage)
            case Failure(exception) =>
              logger.error(s"Unable to execute command ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, "Unable to execute command")
          }
        }
      }
    } ~ path(LongNumber / "stop") { terminalId =>
      put {
          val futureOfSession = SharedSessionCache.getSession(terminalId.toString)
          val stopSession = futureOfSession.map { mayBeSession =>
            mayBeSession match {
              case Some(terminalSession) =>
                val sessions = terminalSession.sshSessions.flatMap(sshSession => sshSession.session)
                val ioExceptions = sessions.foldLeft(List.empty[java.io.IOException]) {
                  (list, session) =>
                    try {
                      session.disconnect()
                      list
                    }
                    catch {
                      case e: java.io.IOException => e :: list
                    }
                }
                if (ioExceptions.nonEmpty) {
                  logger.error(s"Failed due to multiple problems: ", ioExceptions)
                  throw new RuntimeException("Multiple problems", ioExceptions(0))
                }
              case None => logger.warn("There is no open session with id " + terminalId)
            }
            "Successfully stopped session"
          }
        onComplete(stopSession) {
          case Success(successResponse) => complete(StatusCodes.OK, successResponse)
          case Failure(exception) =>
            logger.error(s"Unable to stop Terminal Session. Failed with : ${exception.getMessage}", exception)
            complete(StatusCodes.BadRequest, "Unable to Stop Terminal Session.")
        }
      }
    }
  }

  def generateRandomId: Long = {
    val range = 1234567L
    val randomId = (Random.nextDouble() * range).asInstanceOf[Long]
    randomId
  }

  def startSession(hostProperty: Option[String], instance: Instance, userName: String, keyLocation: String, port: Option[Int],
                   passPhrase: Option[String], sessionTimeout: Int): Option[(String, SSHSession)] = {
    hostProperty match {
      case Some(property) =>
        try {
          val jsch = new JSch
          JSch.setConfig("StrictHostKeyChecking", "no")
          val increasedSessionTimeOut = if (sessionTimeout == -1) Constants.DEFAULT_SESSION_TIMEOUT else sessionTimeout
          passPhrase match {
            case Some(p) => jsch.addIdentity(keyLocation, p)
            case None => jsch.addIdentity(keyLocation)
          }
          val propertyAndSession = if (property == "privateIp") {
            val ip = instance.privateIpAddress
            ip match {
              case Some(privateIp) => Some((property, privateIp, Option(jsch.getSession(userName, privateIp))))
              case None =>
                logger.warn("privateIpAddress not found")
                None
            }
          } else {
            val ip = instance.publicDnsName
            ip match {
              case Some(publicIp) => Some((property, publicIp, Option(jsch.getSession(userName, publicIp))))
              case None =>
                logger.warn("publicDnsName not found")
                None
            }
          }
          propertyAndSession.map { result =>
            val (hostProp, serverIp, mayBeSession) = result
            mayBeSession.foreach { session =>
              session.setTimeout(increasedSessionTimeOut)
              port.foreach(p => session.setPort(p))
              session.connect()
            }
            (hostProp, SSHSession(serverIp, userName, jsch, mayBeSession, keyLocation, port, passPhrase, increasedSessionTimeOut))
          }
        }
        catch {
          case e: JSchException =>
            logger.warn("Failed due to Jsch Exception")
            None
        }
      case None =>
        val session = startSession(Some("privateIp"), instance, userName, keyLocation, port, passPhrase, sessionTimeout)
        if (session.isEmpty) {
          startSession(Some("publicDns"), instance, userName, keyLocation, port, passPhrase, -1)
        } else {
          session
        }
    }
  }

  def buildSSHSession(instance: Instance, hostProp: Option[String]): (String, SSHSession) = {
    val mayBeSSHAccessInfo = instance.sshAccessInfo
    val mayBeKeyPair = mayBeSSHAccessInfo.map(ssh => ssh.keyPair)
    val mayBeKeyFilePath = mayBeKeyPair.flatMap(keyPair => keyPair.filePath)
    val passPhrase = mayBeKeyPair.flatMap(keyPair => keyPair.passPhrase)
    mayBeKeyFilePath match {
      case Some(keyFilePath) =>
        val defaultUser = mayBeKeyPair.flatMap(keyPair => keyPair.defaultUser)
        val mayBeUserName = mayBeSSHAccessInfo.map(ssh => ssh.userName).getOrElse(defaultUser)
        mayBeUserName match {
          case Some(userName) =>
            val port = mayBeSSHAccessInfo.flatMap(ssh => ssh.port)
            val newPort = port.filter(p => p != 0)
            val sessionTimeout = 5 * 1000
            val hostPropAndSSHSession = startSession(hostProp, instance, userName, keyFilePath, newPort, passPhrase, sessionTimeout)
            hostPropAndSSHSession match {
              case Some(hostPropertyAndSSHSession) => hostPropertyAndSSHSession
              case None => throw new RuntimeException("could not start session")
            }
          case None => throw new Exception("userName not found")
        }
      case None => throw new RuntimeException("sshKey not uploaded")
    }
  }

  def parseCommandLine(commandLine: String): List[Command] = {
    val commandMap = Map(Constants.LIST_COMMAND -> new ListCommand,
      Constants.CD_COMMAND -> new ChangeDirectoryCommand,
      Constants.GREP_COMMAND -> new GrepCommand)
    logger.info("Parsing command line [ " + commandLine + "]")
    val commands = commandLine.split("\\|")
    val commandsList = commands.map { command =>
      val cmd = command.trim
      val cmdName = cmd.split("\\s+")(0)
      val argsLine = cmd.substring(cmdName.length).trim
      logger.info(s"Command to be executed [$cmdName] with args and options: $argsLine")
      val pattern = "[^\\s\"']+|\"[^\"]*\"|'[^']*'".r
      val regexMatcher = pattern.findAllMatchIn(argsLine)
      val matchList = regexMatcher.map(m => m.group(0)).toList
      val instanceOfCommand = commandMap(cmdName)
      val jCommander = new JCommander(instanceOfCommand, matchList.toArray: _*)
      instanceOfCommand
    }.toList
    commandsList
  }

  def executePipedCommand(session: TerminalSession, commadLine: String): CommandResult = {
    val commandsList = parseCommandLine(commadLine)
    val executionContext = session.currentCmdExecContext
    val (result, currentContext) = commandsList.foldLeft(List.empty[Line], executionContext) { (inputAndContext, command) =>
      val (input, context) = inputAndContext
      val commandResult = command.execute(context, input)
      commandResult.currentContext match {
        case Some(execContext) => (commandResult.result, execContext)
        case None => throw new Exception(s"did not get execution context while executing the piped command")
      }
    }
    CommandResult(result, Some(currentContext))
  }

  def executeSSHCommand(session: TerminalSession, command: String): CommandResult = {
    val sshSessions = session.sshSessions
    val lines = sshSessions.map { sshSession =>
      val mayBeSession = sshSession.session
      val futureLine = Future {
        val mayBeOutput = mayBeSession.map { session =>
          val channel = session.openChannel("exec").asInstanceOf[ChannelExec]
          channel.setPty(true)
          channel.setCommand(command)
          getOutputFromChannel(channel)
        }
        mayBeOutput match {
          case Some(output) => Line(List(output), LineType.toLineType("RESULT"), Some(sshSession.serverIp))
          case None => throw new Exception("Session not found")
        }
      }
      Await.result(futureLine, Duration(3L, TimeUnit.SECONDS))
    }
    CommandResult(lines, None)
  }

  def getOutputFromChannel(channel: ChannelExec): String = {
    val results = new StringBuilder
    val inputStream = channel.getInputStream
    try {
      channel.connect()
      val buffer = new Array[Byte](1024 * 4)
      // read output till the channel is closed with polling
      while (channel.isConnected) {
        if (inputStream.available > 0) {
          val readCount = inputStream.read(buffer, 0, buffer.length)
          if (readCount > 0) {
            results.append(new java.lang.String(buffer, 0, readCount))
          }
        }
      }
      results.toString
    }
    finally {
      inputStream.close()
    }
  }

  def getSshKeyFilePath(instance: Instance, storedKeyName: String, sshKeyMaterialEntry: String, sshKeyData: String): String = {
    val keyDirPath = "/keys".concat(instance.instanceId.getOrElse("")).concat("/")
    logger.info("Populating ssh key information for key with name [" + storedKeyName + "] to cloud instance [" + instance.instanceId + "]")
    val keyFilePath = keyDirPath.concat(sshKeyMaterialEntry).concat(".pem")
    val temporaryFile = new File(keyDirPath)
    temporaryFile.mkdirs()
    val file = new File(keyFilePath)
    FileUtils.saveContentToFile(file.toString, sshKeyData)
    //TODO: change file permissions to 600
    keyFilePath
  }
}