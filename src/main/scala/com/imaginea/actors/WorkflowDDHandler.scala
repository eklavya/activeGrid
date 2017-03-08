package com.imaginea.actors


import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.{Update,ReadMajority,WriteMajority,Get}
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.util.Timeout
import com.imaginea.Main
import scala.concurrent.duration._


/**
  * Created by sivag on 3/3/17.
  */
sealed trait WrkflwStatus

case object IDLE extends WrkflwStatus

case object RUNNING extends WrkflwStatus

case object INCOMPLETE extends WrkflwStatus

case object REMOVED extends WrkflwStatus

case object START extends WrkflwStatus

case object FAILED extends WrkflwStatus

case class WrkFlow(id: String, operation: String)

class WorkflowDDHandler {

  val replicator = DistributedData(Main.system).replicator
  implicit val node = Cluster(Main.system)


  val mapKey = LWWMapKey[WrkflwStatus]("WorkflowUpdate")
  val readMajority = ReadMajority(5.seconds)
  val writeMajority = WriteMajority(5.seconds)
  implicit val timeout: Timeout = 5.seconds


  def get(wrkFlow: WrkFlow) :WrkflwStatus =  {
    wrkFlow match {
      case WrkFlow(wrkflowId, "START") =>
        val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
        val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
        if (result.contains(wrkflowId)) {
         val update = Update(mapKey, LWWMap.empty[WrkflwStatus], writeMajority)(_.remove(node, wrkflowId))
          START
        }
        else {
          FAILED
        }
      case WrkFlow(wrkflowId, "GETSTATUS") =>
        val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
        val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
        val wrkFlwStatus = result.get(wrkflowId)
        wrkFlwStatus.getOrElse(FAILED)
      case WrkFlow(wrkflowId, "RUNNING") =>
        val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
        val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
        result.get(wrkflowId).getOrElse(FAILED)
      case WrkFlow(wrkflowId, "REMOVE") =>
        val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
        val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
        val wrkFlwStatus = result.get(wrkflowId)
        val isInProgress: Boolean = wrkFlwStatus.equals(RUNNING) || wrkFlwStatus.equals(INCOMPLETE)
        if (isInProgress) {
          FAILED
        }
        else {
          Update(mapKey, LWWMap.empty[WrkflwStatus], writeMajority)(_.put(node, wrkflowId, RUNNING))
          REMOVED
        }
    }
  }
}

