package com.imaginea.activegrid.core.scheduling

import akka.actor.Actor
import com.imaginea.activegrid.core.models.{AutoScalingPolicyEvaluator, PolicyJob}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 7/12/16.
  */
class PolicyJobActor extends Actor {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  override def receive: Receive = {
    case job: PolicyJob =>
      AutoScalingPolicyEvaluator.evaluate(job)
    case _ =>
      logger.info("Unsupported type")
  }
  def evaluate(policyJob: PolicyJob) : Unit = {

  }
}
