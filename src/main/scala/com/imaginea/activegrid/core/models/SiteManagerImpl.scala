package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.imaginea.activegrid.core.utils.ActiveGridUtils


/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {
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

  // Adding new scaling policies
  def  addAutoScalingPolicy(siteId:Long,asPolicy:AutoScalingPolicy) : AutoScalingPolicy = {
    Neo.findNodeByLabelAndId(Site1.label,siteId).foreach{
      site =>
        val policyNode = asPolicy.toNeo4jGraph(asPolicy)
        Neo.createRelation(AutoScalingPolicy.relationLable,site,policyNode)
        val strtDelay = 10000
        val repeatCnt = 5
        val rptIntrvl = 10000
        val uri = ActiveGridUtils.getUriInfo()
       //Code
    }



    asPolicy
  }

}
