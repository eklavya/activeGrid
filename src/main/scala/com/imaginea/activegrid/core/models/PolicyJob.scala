package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}
import org.neo4j.graphdb.Node

/**
  * Created by sivag on 30/11/16.
  */
case class PolicyJob(override val id: Option[Long], autoScalingPolicy: Option[AutoScalingPolicy],
                     siteId: Long, baseUri: String, job: Option[Job]) extends BaseEntity

object PolicyJob {
  val lable = PolicyJob.getClass.getSimpleName;
  val relationLable = AGU.relationLbl(lable)

  def fromNeo4jGraph(id: Long): Option[PolicyJob] = {
    Neo.findNodeByLabelAndId(PolicyJob.lable, id).map {
      pjob =>
        //val siteId = Neo.getProperty[Long](pjob,"siteId") match { case Some(id) => id case _ => 0L}
        val siteId = Neo.getProperty[Long](pjob, "siteId").getOrElse(0L)
        val baseUri = Neo.getProperty[String](pjob, "baseUri").getOrElse("")
        val policyCndn = Neo.getChildNodeId(id, AutoScalingPolicy.relationLable).flatMap {
          id => AutoScalingPolicy.fromNeo4jGraph(id)
        }
        val job = Neo.getChildNodeId(id, Job.relationLable).flatMap {
          id => Job.fromNeo4jGraph(id)
        }
        PolicyJob(Some(pjob.getId), policyCndn, siteId, baseUri, job)
    }
  }

  implicit class RichPolicyJob(policyJob: PolicyJob) extends Neo4jRep[PolicyJob] {
    override def toNeo4jGraph(entity: PolicyJob): Node = {
      val props = Map("siteId" -> entity.siteId, "baseUri" -> entity.baseUri)
      val pjobNode = Neo.saveEntity[PolicyJob](PolicyJob.lable, entity.id, props)
      entity.autoScalingPolicy.foreach {
        aspNode =>
          val node = aspNode.toNeo4jGraph(aspNode)
          Neo.createRelation(PolicyJob.relationLable, pjobNode, node)
      }
      entity.job.foreach {
        jobNode =>
          val node = jobNode.toNeo4jGraph(jobNode)
          Neo.createRelation(Job.relationLable, pjobNode, node)
      }
      pjobNode
    }

    override def fromNeo4jGraph(id: Long): Option[PolicyJob] = {
      PolicyJob.fromNeo4jGraph(id)
    }
  }

}

