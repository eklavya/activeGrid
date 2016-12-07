package com.imaginea.activegrid.core.scheduling

import com.imaginea.activegrid.core.models.{Job, JobType, PolicyJob, Neo4jRepository => Neo}

/**
  * Created by sivag on 1/12/16.
  */
object JobManager {

  // Exsisting jobs.
  def getJobs(): List[Job] = {
    Neo.getNodesByLabel(Job.lable).flatMap(node => Job.fromNeo4jGraph(node.getId))
  }

  // Jobs by type
  def getJobs(jobType: JobType): List[Job] = {
    Neo.getNodesByLabelAndProperty(Job.lable, "jobType", jobType.toString).flatMap(
      node => Job.fromNeo4jGraph(node.getId))
  }

  // Job by name
  def getJob(name: String): Option[Job] = {
    Neo.getSingleNodeByLabelAndProperty(Job.lable, "name", name).flatMap(
      node => Job.fromNeo4jGraph(node.getId))
  }

  // Delete job
  def deleteJob(job: Job): Boolean = {
    job.id match {
      case Some(id) =>
        val mayBeNode = Neo.findNodeByLabelAndId(Job.lable, id)
        mayBeNode.map(node => node.delete())
        mayBeNode.isDefined
      case _ => false
    }
    //job.id.foreach(Neo.findNodeByLabelAndId(Job.lable,_).foreach(_.delete))
  }

  // update Job
  def updateJob(properties: Map[String, Any]): Boolean = {
    val id = properties("id").toString.toLong
    val mayBeNode = Neo.findNodeByLabelAndId(Job.lable, id)
    mayBeNode.foreach {
      jobNode => properties.foreach {
        tple => jobNode.setProperty(tple._1, tple._2)
      }
    }
    mayBeNode.isDefined
  }

  def scheduleJob(policyJob: PolicyJob): Unit = {
    JobSchedular.schedulePolicyJob(policyJob)
  }
}
