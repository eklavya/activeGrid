package com.imaginea.activegrid.core.scheduling

import java.util.concurrent.TimeUnit

import com.imaginea.Main
import com.imaginea.activegrid.core.models.{AutoScalingPolicyEvaluator, PolicyJob}
import com.imaginea.activegrid.core.scheduling.{JobManager => JM}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  * Created by sivag on 2/12/16.
  */
object JobSchedular {
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
        val strtDelay: Long = jobDetails.startDelay.getOrElse(1000L)
        val intrvl: Long = jobDetails.reptIntrvl.getOrElse(60000L)
        //scalastyle:on magic.number
        val scalingPolicyHandler = new Runnable {
          override def run(): Unit = {
            logger.info(s"Evaluating job")
            val policyAplied = Future {
              /**
                * 'blocking' used to ensure that  threads which are available under current
                * execution context shouldn't be blocked  by synchronized block present in call heirarchy or
                * to overcome  shortage in acquring a thread from thread-pool to assign operation.
                */
              scala.concurrent.blocking {
                AutoScalingPolicyEvaluator.evaluate(policyJob)
              }
            }
            //Callback methods on future.
          }
        }
        system.scheduler.schedule(
          FiniteDuration.apply(strtDelay, TimeUnit.MILLISECONDS),
          FiniteDuration.apply(intrvl, TimeUnit.MILLISECONDS),
          scalingPolicyHandler)
    }
  }
}
