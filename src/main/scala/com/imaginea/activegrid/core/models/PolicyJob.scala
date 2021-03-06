package com.imaginea.activegrid.core.models
import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}
import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import org.neo4j.graphdb.Node

/**
  * Created by sivag on 6/12/16.
  */
case class PolicyJob(override val id:Option[Long],
                     siteId:Long,baseUri:Option[String],
                     job: Option[Job],autoScalingPolicy: Option[AutoScalingPolicy]) extends BaseEntity

object PolicyJob
{
  val lable = PolicyJob.getClass.getSimpleName
  val relationLable = AGU.relationLbl(lable)
  def fromNeo4jGraph(id:Long) : Option[PolicyJob] = {
    Neo.findNodeByLabelAndId(PolicyJob.lable,id).map{
      policyJob =>
        //val siteId = Neo.getProperty[Long](policyJob,"siteId") match {case Some(id) => id case _ => 0L}
        val siteId = Neo.getProperty[Long](policyJob,"siteId").getOrElse(0L)
        val baseUri = Neo.getProperty[String](policyJob,"baseUri")
        val job = Neo.getChildNodeId(id,Job.relationLable).flatMap{
          id => Job.fromNeo4jGraph(id)
        }
        val apolicy = Neo.getChildNodeId(id,AutoScalingPolicy.relationLable)flatMap{
          id => AutoScalingPolicy.fromNeo4jGraph(id)
        }
        PolicyJob(Some(id),siteId,baseUri,job,apolicy)
    }
  }
  implicit class RichPolicyJobImpl(policyJob: PolicyJob) extends Neo4jRep[PolicyJob]
  {
    override def toNeo4jGraph(entity: PolicyJob): Node = {
      val props = Map("siteId" -> entity.siteId,"baseUri" -> entity.baseUri)
      val policyJobNode = Neo.saveEntity[PolicyJob](PolicyJob.lable,entity.id,props)
      entity.job.foreach{
        job => Neo.createRelation(Job.relationLable,policyJobNode,job.toNeo4jGraph(job))
      }
      entity.autoScalingPolicy.foreach{
        apolicy=> Neo.createRelation(AutoScalingPolicy.relationLable,policyJobNode,apolicy.toNeo4jGraph(apolicy))
      }
      policyJobNode
    }
    override def fromNeo4jGraph(id: Long): Option[PolicyJob] = {
      PolicyJob.fromNeo4jGraph(id)
    }
  }
}
