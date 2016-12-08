package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.imaginea.activegrid.core.scheduling.{JobManager => JM }
import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}


/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {

  def setAutoScalingGroupSize(siteId: Long, scalingGroupId: Long, scaleSize: Int) = {

  }

  def getAutoScalingPolicies(siteId: Long): List[AutoScalingPolicy] = {
    Site1.fromNeo4jGraph(siteId) match {
      case Some(site) => site.scalingPolicies
      case None => List.empty[AutoScalingPolicy]
    }
  }
  def deleteIntanceFromSite(siteId: Long, instanceId: String): Boolean = {
    val siteNode = Site1.fromNeo4jGraph(siteId)
    siteNode.map { site =>
      //val instance = site.instances.map(instance => instance.id.toString == instanceId)

      //Removing instance  from groups list
      site.groupsList.foreach(instanceGroup => Neo.deleteRelation(instanceId, instanceGroup, "instances"))
      //Need to remove from application.


      //Removing from site
      Neo.deleteRelation(instanceId, site, "instances")
    }
    siteNode.isDefined
  }

  def deletePolicy(policyId: String): Boolean = {
    val mayBePolicy = Neo.findNodeById(policyId.toLong)
    mayBePolicy.map {
      policyNode => policyNode.delete()
    }
    mayBePolicy.isDefined
  }

  def getAutoScalingPolicy(siteId: Long, policyId: String): Option[AutoScalingPolicy] = {
    Neo.findNodeByLabelAndId(AutoScalingPolicy.lable, policyId.toLong).flatMap {
      policyNode => AutoScalingPolicy.fromNeo4jGraph(policyNode.getId)
    }
  }

  // Adding new scaling policy
  def  addAutoScalingPolicy(siteId:Long,policy:AutoScalingPolicy) : AutoScalingPolicy =
  {
    val policyNode = policy.toNeo4jGraph(policy)
    Neo.findNodeByLabelAndId(Site1.label,siteId).foreach { site => Neo.createRelation(AutoScalingPolicy.relationLable,site,policyNode)}
    val startDelay = Some(1000L)
    val reptCount = Some(0)
    val reptIntrvl = Some(1000L)
    val jobType = JobType.convert("POLICY")
    val uriInfo = Some(AGU.getUriInfo())
    //TODO 'name' property have to set from  AutoScaling Policy. Bean declaration required
    val name = "DummyName"
    val job = Job(Some(0L),"PolicyJob",jobType,Some(""),startDelay,reptCount,reptIntrvl,Some(true))
    val pjob = PolicyJob(Some(policyNode.getId),siteId,uriInfo,Some(job),Some(policy))
    JM.scheduleJob(pjob)
    policy
  }

}
