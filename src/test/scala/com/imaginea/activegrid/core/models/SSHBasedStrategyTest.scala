package com.imaginea.activegrid.core.models

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Suite, FlatSpec, PrivateMethodTester}

/**
 * Created by ranjithrajd on 18/11/16.
 */
class SSHBasedStrategyTest extends FlatSpec with PrivateMethodTester with MockFactory with Suite{

  "resolveOS " should "return osinfo tag" in {
    val strategy = SSHBasedStrategy(Builder.topology,List.empty,false)

    val resolveOS = PrivateMethod[List[KeyValueInfo]]('resolveOS)
    val result = strategy.invokePrivate(resolveOS(Some("ubuntu"),Builder.instance1))

    assert(result.size == 1)
    assert(result.contains(KeyValueInfo(None,"osinfo","ubuntu")))

  }

  "toProcessMap " should "return Map of process id and name" in {
    val processList = "1513/mongod;1268/mysqld;5872/java;2880/redis-server;5066/java;"

    val strategy = SSHBasedStrategy(Builder.topology,List.empty,false)

    val toProcessMap = PrivateMethod[Map[String,String]]('toProcessMap)
    val result = strategy.invokePrivate(toProcessMap(processList))

    assert(result.size == 3)
    assert(result.contains("5872")) //mysqld
    assert(!result.contains("2880")) //redis-server
  }

  "connectionResolver " should " takes the process list and instance and return the InstanceConnection" in {
    val processList = "74.125.68.188:;172.217.26.197:;13.107.6.159:;"

    val strategy = SSHBasedStrategy(Builder.topology,List.empty,false)

    val connectionResolver = PrivateMethod[Set[InstanceConnection]]('connectionResolver)
    val result = strategy.invokePrivate(connectionResolver(Some(processList),Builder.instance2))

    assert(result.size == 1)
    val expected = InstanceConnection(None,"i-69c98e7e","i-f6d92ce0",List())
    assert(result.contains(expected))
  }

  "setRole " should " return role tag KeyValueInfo" in {

    val strategy = SSHBasedStrategy(Builder.topology,List.empty,false)

    val setRole = PrivateMethod[Option[KeyValueInfo]]('roleTag)
    val result = strategy.invokePrivate(setRole(Builder.instance2,Builder.software2))
    val expected = KeyValueInfo(None,"roles","WEB")

    assert(result.isDefined)
    assert(result.contains(expected))
  }

  /*Integration test with SSH
  //applicationDetails
  findProcessName
  resolveApplication
  grepInstanceDetails
  resolveProcesses
  */
}
object Builder{
  val instance1 = new Instance(
    id = None,
    instanceId = Some("i-f6d92ce0"),
    name = "74.125.68.188:",
    state = Some("Running"),
    instanceType = Some("t2.small"),
    platform = None,
    architecture = None,
    publicDnsName = None,
    launchTime = None,
    memoryInfo = None,
    rootDiskInfo = None,
    tags = List.empty,
    sshAccessInfo = None,
    liveConnections = List.empty,
    estimatedConnections = List.empty,
    processes = Set.empty,
    image = None,
    existingUsers = List.empty,
    account = None,
    availabilityZone = None,
    privateDnsName = None,
    privateIpAddress = Some("74.125.68.188:"),
    publicIpAddress = Some("74.125.68.188:"),
    elasticIP = None,
    monitoring = None,
    rootDeviceType = None,
    blockDeviceMappings = List.empty,
    securityGroups = List.empty,
    reservedInstance = true,
    region = None)

  val instance2 = new Instance(
    id = None,
    instanceId = Some("i-69c98e7e"),
    name = "172.217.26.197:",
    state = Some("Running"),
    instanceType = Some("t2.small"),
    platform = None,
    architecture = None,
    publicDnsName = None,
    launchTime = None,
    memoryInfo = None,
    rootDiskInfo = None,
    tags = List.empty,
    sshAccessInfo = None,
    liveConnections = List.empty,
    estimatedConnections = List.empty,
    processes = Set.empty,
    image = None,
    existingUsers = List.empty,
    account = None,
    availabilityZone = None,
    privateDnsName = None,
    privateIpAddress = Some("172.217.26.197:"),
    publicIpAddress = Some("172.217.26.197:"),
    elasticIP = None,
    monitoring = None,
    rootDeviceType = None,
    blockDeviceMappings = List.empty,
    securityGroups = List.empty,
    reservedInstance = true,
    region = None)

  val software1 = Software(None,Some("1.0"),"Tomcat","Tomcat",None,"8080",List("Tomcat"),true)
  val software2 = Software(None,Some("5.0"),"PServer","Pramati",None,"9000",List("PServer"),true)

  val softwares = List(software1,software2)

  val site1 = new Site1(
    id = None,
    siteName = "AWS",
    instances = List(instance1),
    reservedInstanceDetails = List.empty,
    filters = List.empty,
    loadBalancers = List.empty,
    scalingGroups = List.empty,
    applications = List.empty,
    groupsList = List.empty,
    applications = List.empty,
    groupBy = "groupBy"
  )
  val topology = new Topology(site1)
}
class SSHSessionWrapper extends SSHSession("127.0.0.0","aws","/tmp",None,None)