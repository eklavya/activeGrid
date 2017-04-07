package com.imaginea.actors
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.util.Timeout
import com.imaginea.Main
import org.slf4j.LoggerFactory
import scala.concurrent.duration._
import akka.pattern._
import Main.system.dispatcher
/**
  * Created by sivag on 3/3/17.
  */
sealed trait WrkflwStatus
case object IDLE extends WrkflwStatus
case object WORKFLOW_RUNNING extends WrkflwStatus
case object INCOMPLETE extends WrkflwStatus
case object WORKFLOW_REMOVED extends WrkflwStatus
case object WORKFLOW_STARTED extends WrkflwStatus
case object WORKFLOW_FAILED extends WrkflwStatus
case object WORKFLOW_NOTFOUND extends WrkflwStatus
case class WrkFlow(id: String, operation: String)

object WorkflowDDHandler {
  val replicator = DistributedData(Main.system).replicator
  implicit val node = Cluster(Main.system)
  val logger = LoggerFactory.getLogger(getClass)
  val mapKey = LWWMapKey[WrkflwStatus]("WorkflowUpdate")
  val readMajority = ReadMajority(5.seconds)
  val writeMajority = WriteMajority(5.seconds)
  implicit val timeout: Timeout = 5.seconds
  //scalastyle:off method.length
  def get(wrkFlow: WrkFlow):Either[WrkflwStatus,LWWMap[WrkflwStatus]] =  {
    wrkFlow match {
        // Removes the purticular workflow from the list.
      case WrkFlow(wrkflowId, "REMOVE") =>
        logger.info("Starting workflow " + wrkflowId)
        val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
        val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
        if (result.contains(wrkflowId)) {
         Update(mapKey, LWWMap.empty[WrkflwStatus], writeMajority)(_.remove(node, wrkflowId))
         val wf = WrkFlow(wrkflowId,"GETSTATUS")
         WorkflowDDHandler.get(wf)
        }
        else {
          Left(WORKFLOW_NOTFOUND)
        }
      // Getting status of the given workflow id.
      case WrkFlow(wrkflowId, "GETSTATUS") =>
        logger.info("Checking status of  workflow " + wrkflowId)
        val data = replicator ? Get(mapKey, readMajority, Some(wrkflowId))
        val d =  data.map {
          case g @ GetSuccess(mapKey,req) =>
                   println("Values from ddmap " + g.dataValue)
                   g.asInstanceOf[LWWMap[WrkflwStatus]].get(wrkflowId)
          case g @ GetFailure(mapKey,req) =>
                   println("Failed to load ddata with key" + mapKey)
                   Left(WORKFLOW_FAILED)
          case g @ NotFound(mapKey,req) =>
                   println("Key not found ::" + mapKey)
                   Left(WORKFLOW_NOTFOUND)
        }
        Left(WORKFLOW_FAILED)
      // Returns all running workflows.
      case WrkFlow(wrkflowId, "RUNNING_WORKFLOWS") =>
        logger.info("Fetching all running workflows " + wrkflowId)
        val getStatus = Get(mapKey, readMajority, Some(wrkflowId))
        val result = getStatus.asInstanceOf[LWWMap[WrkflwStatus]]
        Right(result)
      //Chagning status to running.
      case WrkFlow(wrkflowId, "START") =>
        logger.info("Removing workflow " + wrkflowId)
        val wf = WrkFlow(wrkflowId,"GETSTATUS")
        WorkflowDDHandler.get(wf) match {
          case Left(x) =>
            if(x.equals(WORKFLOW_NOTFOUND)) {
              Update(mapKey, LWWMap.empty[WrkflwStatus], writeMajority)(_.put(node, wrkflowId, WORKFLOW_RUNNING))
            }
            WorkflowDDHandler.get(wf)
          case _ =>  Left(WORKFLOW_RUNNING)
        }
    }
  }
}


