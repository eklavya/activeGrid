package com.activegrid.model


import akka.http.scaladsl.model.headers.Date
import com.activegrid.model.KeyPairStatus.KeyPairStatus


/**
  * Created by shareefn on 27/9/16.
  */

case class Site(instances: List[Instance]) extends BaseEntity

case class Instance(instanceId: String,
                    name: String,
                    state: String,
                    platform: String,
                    architecture: String,
                    publicDnsName: String,
                    launchTime: Date,
                    memoryInfo: StorageInfo,
                    rootDiskInfo: StorageInfo,
                    tags: List[Tuple],
                    sshAccessInfo: SSHAccessInfo,
                    liveConnections: List[InstanceConnection],
                    estimatedConnections: List[InstanceConnection],
                    processes: Set[ProcessInfo],
                    image: ImageInfo,
                    existingUsers: List[InstanceUser]
                   ) extends BaseEntity

case class StorageInfo(used: Double, total: Double) extends BaseEntity

case class Tuple(key: String, value: String)  extends BaseEntity

case class SSHAccessInfo(keyPair : KeyPairInfo, userName: String, port: Int)  extends BaseEntity

case class InstanceConnection(sourceNodeId: String, targetNodeId: String, portRanges: List[PortRange])  extends BaseEntity

case class ProcessInfo(pid: Int, parentPid: Int, name: String, command: String, owner: String, residentBytes: Long, software: Software, softwareVersion: String)  extends BaseEntity

case class InstanceUser(userName: String, publicKeys: List[String])  extends BaseEntity


case class PortRange(fromPort: Int, toPort: Int)  extends BaseEntity

case class Software(version: String, name: String, provider: String, downloadURL: String, port: String, processNames: List[String],discoverApplications: Boolean)  extends BaseEntity

case class KeyPairInfo(keyName: String,keyFingerprint: String,keyMaterial: String, filePath:String, status: KeyPairStatus, defaultUser: String, passPhrase: String )  extends BaseEntity

object KeyPairStatus extends Enumeration{
  type KeyPairStatus = Value
  val UPLOADED, NOT_YET_UPLOADED, INCORRECT_UPLOAD = Value
}


/*
case class Site(siteName: String,
                instances: List[Instance],
                filters: List[SiteFilter],
                keypairs: List[KeyPairInfo],
                groupsList: List[InstanceGroup],
                applications: List[Application],
                groupBy: String,
                loadBalancers: List[LoadBalancer],
                scalingGroups: List[ScalingGroup],
                reservedInstanceDetails: List[ReservedInstanceDetails],
                scalingPolicies: List[AutoScalingPolicy]



case class KeyPairStatus()




*/