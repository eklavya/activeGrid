package com.imaginea.activegrid.core.discovery.models

import java.util.Date

import com.imaginea.activegrid.core.models._

/**
 * Created by ranjithrajd on 10/10/16.
 */
class Instance(instanceId: String,
               name: String,
               state: String,
               platform: String,
               architecture: String,
               publicDnsName: String) extends BaseEntity{
  val launchTime: Date = new Date()
  val memoryInfo: StorageInfo = null
  val rootDiskInfo: StorageInfo = null
  val tags : List[(String,String)] = null
  val sshAccessInfo : SSHAccessInfo = null
  val liveConnections:  List[InstanceConnection] = null
  val estimatedConnections :  List[InstanceConnection] = null
  val processes: Set[ProcessInfo] = null
  val ImageInfo: ImageInfo = null
  val existingUsers: List[InstanceUser] = List.empty

}

case class StorageInfo(used: Double,total: Double) extends BaseEntity

case class SSHAccessInfo(keyPairkey: KeyPairInfo,keyName: String,port : Option[Int] = None) extends BaseEntity{
  def this(keyName: String){
    this(new KeyPairInfo(None,keyName, null, null, null,KeyPairStatus.NOT_YET_UPLOADED,null,null),null)
  }
}

case class InstanceConnection(sourceNodeId: String,targetNodeId: String,portRanges: List[PortRange]) extends BaseEntity{
  def this(sourceId: String,targetNodeId: String){
    this(sourceId,targetNodeId,List.empty)
  }
}

case class PortRange(fromPort: Int,toPort: Int) extends BaseEntity{

  def containsPort(port: Int) = port >= fromPort && port <= toPort

  override def toString: String = "RangeOfPorts [fromPort=" + fromPort + ", toPort=" + toPort + "]"
}

case class InstanceFlavor(name: String,cpuCount: Int,memory: Double,rootDisk: Double) extends BaseEntity

case class ProcessInfo(pid: Int,parentPid: Int,name: String,software: Software,softwareVersion: String) extends BaseEntity{

  val command: Option[String] = None
  val residentBytes: Option[Long] = None
}

case class Software(version: String,name: String,provider: String,downloadURL: String,
                     port: String,
                    processNames: Array[String],
                    discoverApplications: Boolean) extends BaseEntity
