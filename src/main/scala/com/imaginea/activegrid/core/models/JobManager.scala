package com.imaginea.activegrid.core.models
import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}

/**
  * Created by sivag on 1/12/16.
  */
object JobManager {

  // Exsisting jobs.
  def getJobs() : List[Job] = {
    Neo.getNodesByLabel(Job.lable).flatMap(node => Job.fromNeo4jGraph(node.getId))
  }
  // Jobs by type
  def getJobs(jobType: JobType): List[Job] ={
    Neo.getNodesByLabelAndProperty(Job.lable,"jobType",jobType.toString).flatMap(
      node => Job.fromNeo4jGraph(node.getId))
  }
  // Job by name
  def getJob(name:String) : Option[Job] = {
    Neo.getSingleNodeByLabelAndProperty(Job.lable,"name",name).flatMap(
      node => Job.fromNeo4jGraph(node.getId))
  }
  // Delete job
  def deleteJob(job: Job): Boolean ={
    job.id match {
      case Some(id) =>
        val mayBeNode = Neo.findNodeByLabelAndId(Job.lable,id)
        mayBeNode.map(node => node.delete())
        mayBeNode.isDefined
      case _ => false
    }
    //job.id.foreach(Neo.findNodeByLabelAndId(Job.lable,_).foreach(_.delete))
  }
  // update Job
  def updateJob(job: Job) : Boolean = {

    // Need to write.
    true

  }
  def scheduleJob(job: Job) : Unit = {
    //Need to implement
  }
}
