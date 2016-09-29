package com.activegrid.model


import akka.http.scaladsl.model.headers.Date


/**
  * Created by shareefn on 27/9/16.
  */

case class Site(instances: List[String], d_name:String, d_id: Int) extends BaseEntity



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
                   )

case class StorageInfo(used: Double, total: Double)

case class Tuple(key: String, value: String)

case class SSHAccessInfo(keyPair : KeyPairInfo, userName: String, port: Int)

case class InstanceConnection(sourceNodeId: String, targetNodeId: String, portRanges: List[PortRange])

case class ProcessInfo(pid: Int, parentPid: Int, name: String, command: String, owner: String, residentBytes: Long, software: Software, softwareVersion: String)

case class InstanceUser(userName: String, publicKeys: List[String])

case class KeyPairInfo(keyName: String,keyFingerprint: String,keyMaterial: String, filePath:String, status: KeyPairStatus, defaultUser: String, passPhrase: String )

case class PortRange(fromPort: Int, toPort: Int)

case class Software(version: String, name: String, provider: String, downloadURL: String, port: String, processNames: List[String],discoverApplications: Boolean)

case class KeyPairStatus()

*/