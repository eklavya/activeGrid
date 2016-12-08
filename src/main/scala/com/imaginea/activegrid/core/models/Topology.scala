package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 2/11/16.
 */
case class Topology(site: Site1,
                    keyNames : Set[String] = Set.empty,
                    nodes: List[Instance]
                    ) {
  val idVsInstance = nodes.map(node => (node.instanceId.get -> node)).toMap

  def getInstanceByIp(serverIp: String ): Option[Instance] = {
    val instanceResult = nodes.find(instance => {
      val privateIpAddress = instance.privateIpAddress
      val publicIpAddress = instance.publicIpAddress
      privateIpAddress.equals(serverIp) || publicIpAddress.equals(serverIp) || instance.name.equals(serverIp)
    })
    instanceResult
  }
}