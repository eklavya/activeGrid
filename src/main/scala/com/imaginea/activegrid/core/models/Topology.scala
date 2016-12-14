package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 2/11/16.
 */
case class Topology(site: Site1,
                    keyNames : Set[String],
                    nodes: List[Instance],
                    idVsInstance : Map[String,Instance]
                    ) {
  def this(site: Site1)={
    this(
      site,
      Set.empty,
      nodes = site.instances,
      idVsInstance = (for{ node <- site.instances; instanceId <- node.instanceId } yield (instanceId -> node)).toMap
    )
  }

  def getInstanceByIp(serverIp: String ): Option[Instance] = {
    val instanceResult = nodes.find(instance => {
      val privateIpAddress = instance.privateIpAddress
      val publicIpAddress = instance.publicIpAddress
      privateIpAddress.equals(serverIp) || publicIpAddress.equals(serverIp) || instance.name.equals(serverIp)
    })
    instanceResult
  }
}