package com.imaginea.activegrid.core.scheduling

import java.util.concurrent.TimeUnit

import com.imaginea.Main
import com.imaginea.activegrid.core.models.{AutoScalingPolicyEvaluator, PolicyJob}
import com.imaginea.activegrid.core.scheduling.{JobManager => JM}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration

/**
  * Created by sivag on 2/12/16.
  */
object JobSchedular
{
  val logger = LoggerFactory.getLogger(JobSchedular.getClass)
  def schedule(): Unit =
  {
    val jobs = JM.getJobs()
    //    TODO job specific handling requried.
  }
  def schedulePolicyJob(policyJob: PolicyJob): Unit = {
    implicit val system = Main.system
    implicit val materializer = Main.materializer
    implicit val executionContext = Main.executionContext
    policyJob.job.foreach {
      jobDetails =>
        val strtDelay: Long = jobDetails.startDelay.getOrElse(1000)
        val intrvl: Long = jobDetails.reptIntrvl.getOrElse(60000)
        val scalingPolicyHandler = new Runnable {
          override def run(): Unit = {
            logger.info(s"Evaluating job")
            AutoScalingPolicyEvaluator.evaluate(policyJob)
          }
        }
        system.scheduler.schedule(
          FiniteDuration.apply(strtDelay, TimeUnit.MILLISECONDS),
          FiniteDuration.apply(intrvl, TimeUnit.MILLISECONDS),
          scalingPolicyHandler)
    }
  }
}
