package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}
import org.neo4j.graphdb.Node

/**
  * Created by sivag on 29/11/16.
  */
case class Job(override val id: Option[Long], name: String, jobType: JobType, cronExpr: Option[String],
               startDelay: Option[Long], reptCount: Option[Int], reptIntrvl: Option[Long], acive: Option[Boolean])
  extends BaseEntity

object Job {
  val lable = Job.getClass.getSimpleName
  val relationLable = AGU.relationLbl(lable)

  def fromNeo4jGraph(id: Long): Option[Job] = {
    Neo.findNodeByLabelAndId(Job.lable, id).map {
      jobNode =>
        val props = Neo.getProperties(jobNode, "id", "name", "jobType",
          "cronExpr", "startDelay", "repeatCount", "repeatIntrvl", "active")

        val name = Neo.getProperty[String](jobNode, "name").getOrElse("Not provided")
        val jobType = JobType.convert(Neo.getProperty[String](jobNode, "jobType").getOrElse(""))
        val cronExpr = Neo.getProperty[String](jobNode, "cronExpr")
        val startDelay = Neo.getProperty[Long](jobNode, "startDelay")
        val repeteCount = Neo.getProperty[Int](jobNode, "repeatCount")
        val repeatIntrvl = Neo.getProperty[Long](jobNode, "repeatIntrvl")
        val active = Neo.getProperty[Boolean](jobNode, "active")

        Job(Some(id), name, jobType, cronExpr, startDelay, repeteCount, repeatIntrvl, active)


    }
  }

  implicit class RichJob(job: Job) extends Neo4jRep[Job] {
    override def toNeo4jGraph(entity: Job): Node = {
      val props = Map("name" -> entity.name, "jobType" -> entity.jobType.toString,
        "cronExpr" -> entity.cronExpr, "startDelay" -> entity.startDelay,
        "repeatCount" -> entity.reptCount, "repeatIntrvl" -> entity.reptIntrvl,
        "active" -> entity.acive)
      Neo.saveEntity[Job](Job.lable, job.id, props)
    }

    override def fromNeo4jGraph(id: Long): Option[Job] = {
      Job.fromNeo4jGraph(id)
    }
  }

}

