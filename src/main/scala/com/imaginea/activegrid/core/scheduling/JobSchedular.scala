package com.imaginea.activegrid.core.scheduling

import java.util.concurrent.TimeUnit

import akka.actor.Props
import com.imaginea.Main
import com.imaginea.activegrid.core.models.PolicyJob
import com.imaginea.activegrid.core.scheduling.{JobManager => JM}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
/**
  * Created by sivag on 2/12/16.
  */
object JobSchedular
{

  val logger = LoggerFactory.getLogger(JobSchedular.getClass)

  def schedule(): Unit = {
    val jobs = JM.getJobs()
    //    TODO job specific handling requried.
  }

  def schedulePolicyJob(policyJob: PolicyJob): Unit = {
    implicit val system = Main.system
    implicit val materializer = Main.materializer
    implicit val executionContext = Main.executionContext
    policyJob.job.foreach {
      jobDetails =>
        //scalastyle:off magic.number
        val strtDelay : Long = jobDetails.startDelay match {
          case Some(v) => v
          case None => 1000
        }
        val intrvl : Long = jobDetails.reptIntrvl match {
          case Some(v) => v
          case  None => 1000
        }
        //scalastyle:on magic.number
        val triggerHandlingActor = system.actorOf(Props(classOf[PolicyJobActor]))
        system.scheduler.schedule(
          FiniteDuration.apply(strtDelay, TimeUnit.MILLISECONDS),
          FiniteDuration.apply(intrvl, TimeUnit.MILLISECONDS),
          triggerHandlingActor,
          policyJob)

    }
  }
}
