package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.policy.models.AutoScalingPolicy
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 10/10/16.
 */

case class Site( override val id: Option[Long],
           siteName: String,
           groupBy: String
            ) extends BaseEntity

//TODO: The above case class is the Simple Site used to persist
// Waiting for Site Service to bring the Site Entity case clas
/*case class Site( override val id: Option[Long],
                 siteName: String,
                 instances: List[Instance] = List.empty,
                 filters: List[Filter] = List.empty,
                 keypairs: List[InstanceGroup] = List.empty,
                 applications: List[Application] = List.empty,
                 groupBy: String,
           loadBalancers: List[LoadBalancer] = List.empty,
           scalingGroups: List[ScalingGroup] = List.empty,
           reservedInstanceDetails: List[ReservedInstanceDetails] = List.empty,
           scalingPolicies: List[AutoScalingPolicy] = List.empty
                 ) extends BaseEntity*/

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


    override def toNeo4jGraph(site: Site): Option[Node] = {

      logger.debug(s"Site Node saved into db - ${site}")
      val map = Map("name" -> site.siteName,"groupBy" -> site.groupBy)

      val siteNode = Neo4jRepository.saveEntity[Site](label, site.id, map)

     /* //map function is used to extract the option value
      //Iterating the users and linking to the UserGroup
      logger.debug(s"Instances has relation with Site ${site.siteName}")

      for {
           instance <- site.instances
           userNode <- instance.toNeo4jGraph(instance)} {
        Neo4jRepository.createRelation(has_users, ugn, userNode)
      }

      //map function is used to extract the option value
      //Iterating the access and linking to the UserGroup
      logger.debug(s"UserGroupProxy has relation with ResourceACL ${userGroup.accesses}")
      for {accesses <- userGroup.accesses
           resource <- accesses
           ugn <- userGroupNode
           resourceNode <- resource.toNeo4jGraph(resource)} {
        Neo4jRepository.createRelation(has_resourceAccess, ugn, resourceNode)
      }*/

      siteNode
    }


    override def fromNeo4jGraph(nodeId: Long): Option[Site] = {
      val nodeOption = Neo4jRepository.findNodeById(nodeId)

      nodeOption.map(node => {
        logger.debug(s" SiteNode ${node}")

        val userGroupMap = Neo4jRepository.getProperties(node, "name")

       /* val userNodes = Neo4jRepository.getNodesWithRelation(node, has_users)
        val users: Set[User] = userNodes.map(child => {
          logger.debug(s" UserGroup -> User node ${child}")
          val user: User = null
          user.fromNeo4jGraph(child.getId)
        }).flatten.toSet

        val accessNodes = Neo4jRepository.getNodesWithRelation(node, has_resourceAccess)
        val resources = accessNodes.map(child => {
          logger.debug(s" UserGroup -> Resource node ${child}")
          val resource: ResourceACL = null
          resource.fromNeo4jGraph(child.getId)
        }).flatten.toSet
*/
        val site = Site(
         id = Some(node.getId),
         siteName = userGroupMap.get("name").get.asInstanceOf[String],
         groupBy = userGroupMap.get("groupBy").get.asInstanceOf[String]
        )
        logger.debug(s"Site - ${site}")
        site
      })
    }
  }
}
