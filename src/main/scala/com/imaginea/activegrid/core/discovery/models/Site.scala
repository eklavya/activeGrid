package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models._
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 10/10/16.
 */

case class Site(override val id: Option[Long],
                siteName: String,
                groupBy: String
                 ) extends BaseEntity

//TODO: The above case class is the Simple Site used to persist
//It will be replaced once the SiteService up

object Site {
  val label = "Site"

  implicit class RichSite(site: Site) extends Neo4jRep[Site] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    val hasInstances = "HAS_instances"
    val hasKeypairs = "HAS_keypairs"
    val hasApplications = "HAS_applications"
    val hasLoadBalancers = "HAS_loadBalancers"
    val hasScalingGroups = "HAS_scalingGroups"
    val hasReservedInstanceDetails = "HAS_reservedInstanceDetails"
    val hasScalingPolicies = "HAS_scalingPolicies"


    override def toNeo4jGraph(site: Site): Node = {

      logger.debug(s"Site Node saved into db - ${site}")
      val map = Map("name" -> site.siteName, "groupBy" -> site.groupBy)

      val siteNode = Neo4jRepository.saveEntity[Site](label, site.id, map)

      siteNode
    }


    override def fromNeo4jGraph(nodeId: Long): Option[Site] = {
      val siteNode = Neo4jRepository.findNodeById(label,nodeId)

      logger.debug(s" SiteNode ${siteNode}")

      val userGroupMapOption = Neo4jRepository.getProperties(siteNode, "name","groupBy")
      userGroupMapOption.map(userGroupMap => {
        val site = Site(
          id = Some(siteNode.getId),
          siteName = userGroupMap.get("name").get.asInstanceOf[String],
          groupBy = userGroupMap.get("groupBy").get.asInstanceOf[String]
        )
        logger.debug(s"Site - ${site}")
        site
      }).orElse(None)
    }
  }

}
