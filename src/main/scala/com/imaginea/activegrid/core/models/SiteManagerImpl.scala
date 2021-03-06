package com.imaginea.activegrid.core.models

import com.amazonaws.regions.RegionUtils
import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.imaginea.activegrid.core.scheduling.{JobManager => JM}
import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}


/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {

  /**
    *
    * @param authStratagy
    * @return Three authorization levels defined in application
    *         1. Anonymous
    *         2. Admin
    *         3. Registered users
    *         This method will configure headers according to given authorization level
    */
  def getAuthSettingsFor(authStratagy: String): String = {
   AppSettings.getAuthSettingsFor(authStratagy)
  }

  /**
    *
    * @param siteId
    * @param scalingGroupId
    * @param scaleSize value always be either  -1 or 1
    *                  Increase/decrease number of instances in an account without exceeding both extrems
    *                  It uses AWS Auto-scaling request
    */
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

  /**
    *
    * @param siteId
    * @return
    *         Returns scaling policy registered under 'siteId', Empty returned in case if there is no policies
    *         registered with given siteId
    */
  def getAutoScalingPolicies(siteId: Long): List[AutoScalingPolicy] = {
    Site1.fromNeo4jGraph(siteId) match {
      case Some(site) => site.scalingPolicies
      case None => List.empty[AutoScalingPolicy]
    }
  }

  /**
    *
    * @param siteId
    * @param instanceId
    * @return
    *
    */
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

  /**
    *
    * @param siteId
    * @param policyId
    * @return
    */
  def getAutoScalingPolicy(siteId: Long, policyId: String): Option[AutoScalingPolicy] = {
    Neo.findNodeByLabelAndId(AutoScalingPolicy.lable, policyId.toLong).flatMap {
      policyNode => AutoScalingPolicy.fromNeo4jGraph(policyNode.getId)
    }
  }

  /**
    *
    * @param siteId
    * @param policy
    * @return
    *         It does the following
    *         1. It save the policy object into Neo4j for persistance and later usuages
    *         2. Creaet Job using policy details
    *         3. Schedule that job with JobManager to apply the policy.
    */
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
    val job = Job(Some(0L), name, jobType, Some(""), startDelay, reptCount, reptIntrvl, Some(true), None, None)
    val pjob = PolicyJob(Some(policyNode.getId), siteId, uriInfo, Some(job), Some(policy))
    JM.scheduleJob(pjob)
    policy
  }

}
