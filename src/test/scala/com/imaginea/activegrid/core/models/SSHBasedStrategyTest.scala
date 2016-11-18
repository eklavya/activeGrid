package com.imaginea.activegrid.core.models

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, PrivateMethodTester}

/**
 * Created by ranjithrajd on 18/11/16.
 */
class SSHBasedStrategyTest extends FlatSpec with PrivateMethodTester with MockFactory{

  "ResolveOS " should "return osinfo tag" in {
    val strategy = SSHBasedStrategy(Builder.topology,List.empty,false)

    val resolveOS = PrivateMethod[List[KeyValueInfo]]('resolveOS)
    val result = strategy.invokePrivate(resolveOS(Some("ubuntu"),Builder.instance))

    assert(result.size == 1)
    assert(result.contains(KeyValueInfo(None,"osinfo","ubuntu")))

  }

  "ToProcessMap " should "return Map of process id and name" in {
    val processList = "1513/mongod;1268/mysqld;5872/java;2880/redis-server;5066/java;"

    val strategy = SSHBasedStrategy(Builder.topology,List.empty,false)

    val toProcessMap = PrivateMethod[Map[String,String]]('toProcessMap)
    val result = strategy.invokePrivate(toProcessMap(processList))
    println(result)
    assert(result.size == 3)

    assert(result.contains("5872")) //mysqld
    assert(!result.contains("2880")) //redis-server
  }

}
object Builder{
  val site1 = new Site1(
    id = None,
    siteName = "AWS",
    instances = List.empty,
    reservedInstanceDetails = List.empty,
    filters = List.empty,
    loadBalancers = List.empty,
    scalingGroups = List.empty,
    applications = List.empty,
    groupsList = List.empty
  )
  val topology = new Topology(site1)

  val instance = new Instance(
    id = None,
    instanceId = Some("i-f6d92ce0"),
    name = "AWS",
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
    privateIpAddress = None,
    publicIpAddress = None,
    elasticIP = None,
    monitoring = None,
    rootDeviceType = None,
    blockDeviceMappings = List.empty,
    securityGroups = List.empty,
    reservedInstance = true,
    region = None)
}
class SSHSessionWrapper extends SSHSession("127.0.0.0","aws","/tmp",None,None)