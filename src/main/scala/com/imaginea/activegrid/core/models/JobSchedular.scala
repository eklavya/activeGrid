package com.imaginea.activegrid.core.models
import java.util.Date

import akka.actor.ActorSelection
import com.imaginea.activegrid.core.models.{JobManager => JM}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


/**
  * Created by sivag on 2/12/16.
  */
object JobSchedular {

  val logger = LoggerFactory.getLogger(JobSchedular.getClass)

  def schedule() : Unit ={
     val jobs =  JM.getJobs()
  }

  def schedule(name: String, receiver: ActorSelection, msg: AnyRef, startDate: Option[Date]): java.util.Date = {
    new java.util.Date("")
  }


}
