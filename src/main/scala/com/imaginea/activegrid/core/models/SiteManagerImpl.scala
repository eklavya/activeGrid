package com.imaginea.activegrid.core.models

import com.amazonaws.regions.RegionUtils
import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.imaginea.activegrid.core.scheduling.{JobManager => JM}
import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}


/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {
  def getAuthSettingsFor(authMechanizm: String): String = {
    // Get auth settings and return the respective fields that requred for "authMechanizm"
    "dummyResponse"
  }

  def getInstance(siteId: Long, instanceId: String): APMProvider = {
    //todo implementation required.
    APMProvider.toProvider("")
  }

  def setAutoScalingGroupSize(siteId: Long, scalingGroupId: Long, scaleSize: Int): Unit = {
    Site1.fromNeo4jGraph(siteId).foreach {
      site =>
        val targetScalingGroups = site.scalingGroups.filter(sgroup => sgroup.id.getOrElse(0L) == scalingGroupId)
        targetScalingGroups.foreach { sgroup =>
          val desiredSize = sgroup.desiredCapacity + scaleSize
          if (sgroup.minCapacity <= desiredSize && desiredSize <= sgroup.maxCapacity) {
            site.filters.foreach { f =>
              val accountInfo = f.accountInfo
              val regionName = accountInfo.regionName.getOrElse("")
              val credentials = AWSComputeAPI.getCredentials(accountInfo, regionName)
              val region = RegionUtils.getRegion(regionName)
              val awsScalingClinet = AWSComputeAPI.getAWSAutoScalingPolicyClient(credentials, region)
              AWSComputeAPI.applyScalingSize(awsScalingClinet, sgroup.name, desiredSize)
            }
          }
        }
    }
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
  def addAutoScalingPolicy(siteId: Long, policy: AutoScalingPolicy): AutoScalingPolicy = {
    val policyNode = policy.toNeo4jGraph(policy)
    Neo.findNodeByLabelAndId(Site1.label, siteId).foreach { site =>
      Neo.createRelation(AutoScalingPolicy.relationLable, site, policyNode)
    }
    //scalastyle:off magic.number
    val startDelay = Some(1000L)
    val reptCount = Some(0)
    val reptIntrvl = Some(6000L)
    //scalastyle:on magic.number
    val jobType = JobType.convert("POLICY")
    val uriInfo = Some(AGU.getUriInfo())
    // refer https://github.com/eklavya/activeGrid/issues/64
    val name = policy.name
    val job = Job(Some(0L), name, jobType, Some(""), startDelay, reptCount, reptIntrvl, Some(true))
    val pjob = PolicyJob(Some(policyNode.getId), siteId, uriInfo, Some(job), Some(policy))
    JM.scheduleJob(pjob)
    policy
  }

}
