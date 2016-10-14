package com.imaginea.activegrid.core.discovery.models

import java.util.Date

import com.imaginea.activegrid.core.models._

/**
 * Created by ranjithrajd on 10/10/16.
 */
case class Instance(override val id: Option[Long],
                    instanceId: String,
                    name: String,
                    state: String,
                    platform: String,
                    architecture: String,
                    publicDnsName: String,
                    launchTime: Date = new Date(),
                    memoryInfo: Option[StorageInfo] = None,
                    rootDiskInfo: Option[StorageInfo] = None,
                    tags: List[(String, String)] = List.empty,
                    sshAccessInfo: Option[SSHAccessInfo] = None,
                    liveConnections: List[InstanceConnection] = List.empty,
                    estimatedConnections: List[InstanceConnection] = List.empty,
                    processes: Set[ProcessInfo] = Set.empty,
                    ImageInfo: Option[ImageInfo] = None,
                    existingUsers: List[InstanceUser] = List.empty
                     ) extends BaseEntity


case class StorageInfo(override val id: Option[Long]
                       , used: Double
                       , total: Double) extends BaseEntity

case class SSHAccessInfo(override val id: Option[Long]
                         , keyPairKey: KeyPairInfo
                         , keyName: Option[String] = None
                         , port: Option[Int] = None
                          ) extends BaseEntity {
  def this(keyName: String) {
    this(None, new KeyPairInfo(id = None, keyName = keyName, status = NotYetUploadedKeyPair))
  }
}

case class InstanceConnection(override val id: Option[Long]
                              , sourceNodeId: String
                              , targetNodeId: String
                              , portRanges: List[PortRange]) extends BaseEntity {
  def this(sourceId: String, targetNodeId: String) {
    this(None, sourceId, targetNodeId, List.empty)
  }
}

case class PortRange(override val id: Option[Long],
                     fromPort: Int, toPort: Int) extends BaseEntity {

  def containsPort(port: Int) = port >= fromPort && port <= toPort

  override def toString: String = "RangeOfPorts [fromPort=" + fromPort + ", toPort=" + toPort + "]"
}

case class InstanceFlavor(override val id: Option[Long],
                          name: String,
                          cpuCount: Int,
                          memory: Double,
                          rootDisk: Double) extends BaseEntity

case class ProcessInfo(override val id: Option[Long]
                       , pid: Int
                       , parentPid: Int
                       , name: String
                       , software: Software
                       , softwareVersion: String) extends BaseEntity {

  val command: Option[String] = None
  val residentBytes: Option[Long] = None
}

case class Software(override val id: Option[Long]
                    , version: String
                    , name: String
                    , provider: String
                    , downloadURL: String
                    , port: String
                    , processNames: Array[String]
                    , discoverApplications: Boolean) extends BaseEntity
