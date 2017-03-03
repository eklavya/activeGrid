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

 _

  type T = WrkflwStatus
  val mapKey = LWWMapKey[T]("WorkflowUpdate")
  val readMajority = ReadMajority(5.seconds)
  val writeMajority = WriteMajority(5.seconds)
  implicit val timeout: Timeout = 5.seconds


  override def receive: Receive = {

    case WrkFlow(wrkflowId, "START") =>
      val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[T]]
      if (result.contains(wrkflowId)) {
        val up = Update(mapKey, LWWMap.empty[T], writeMajority)(_.remove(node, wrkflowId))
        sender() ! up.asInstanceOf[UpdateSuccess[LWWMapKey[T]]]
      }
      else {
        sender() ! FAILED
      }
    case WrkFlow(wrkflowId, "GETSTATUS") =>
      val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[T]]
      val wrkFlwStatus = result.get(wrkflowId)
      sender() ! wrkFlwStatus.equals(RUNNING)
    case WrkFlow(wrkflowId, "RUNNING") =>
      val getStatus = replicator ? Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[T]]
      sender() ! result
    case WrkFlow(wrkflowId, "REMOVE") =>
      val getStatus = replicator ? Get(mapKey, readMajority, Some(wrkflowId))
      val result = getStatus.asInstanceOf[LWWMap[T]]
      val wrkFlwStatus = result.get(wrkflowId)
      if (wrkFlwStatus.equals(RUNNING) || wrkFlwStatus.equals(INCOMPLETE)) {
        sender() ! FAILED
      }
      else {
        val up = Update(mapKey, LWWMap.empty[T], writeMajority)(_.put(node, wrkflowId, RUNNING))
        sender() ! up.asInstanceOf[UpdateSuccess[LWWMapKey[T]]]
      }

  }

}

