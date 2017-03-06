package com.imaginea.actors


import scala.concurrent.duration._
import akka.cluster.ddata.Replicator._
import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.pattern.ask
import akka.util.Timeout


/**
  * Created by sivag on 3/3/17.
  */
sealed trait WrkflwStatus

case object IDLE extends WrkflwStatus

case object RUNNING extends WrkflwStatus

case object INCOMPLETE extends WrkflwStatus

case object START extends WrkflwStatus

case object FAILED extends WrkflwStatus

case class WrkFlow(id: String, operation: String)

class WofklowActor extends Actor {

  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)


  val mapKey = LWWMapKey[WrkflwStatus]("WorkflowUpdate")
  val readMajority = ReadMajority(5.seconds)
  val writeMajority = WriteMajority(5.seconds)
  implicit val timeout: Timeout = 5.seconds


  override def receive: Receive = {

    case WrkFlow(wrkflowId, "START") =>
      val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
      if (result.contains(wrkflowId)) {
        sender() ! Update(mapKey, LWWMap.empty[WrkflwStatus], writeMajority)(_.remove(node, wrkflowId))
      }
      else {
        sender() ! FAILED
      }
    case WrkFlow(wrkflowId, "GETSTATUS") =>
      val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
      val wrkFlwStatus = result.get(wrkflowId)
      sender() ! wrkFlwStatus.equals(RUNNING)
    case WrkFlow(wrkflowId, "RUNNING") =>
      val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
      sender() ! result
    case WrkFlow(wrkflowId, "REMOVE") =>
      val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
      val wrkFlwStatus = result.get(wrkflowId)
      if (wrkFlwStatus.equals(RUNNING) || wrkFlwStatus.equals(INCOMPLETE)) {
        sender() ! FAILED
      }
      else
        sender() ! Update(mapKey, LWWMap.empty[WrkflwStatus], writeMajority)(_.put(node, wrkflowId, RUNNING))
      }
  }

