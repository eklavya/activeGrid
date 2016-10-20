package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.discovery.models.{Instance, Site}
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 10/10/16.
 */
case class SiteACL(override val id: Option[Long]
                   , name: String
                   , site: Option[Site]
                   , instances: List[Instance]
                   , groups: List[UserGroup]) extends BaseEntity {
}

object SiteACL {
  val label = "SiteACL"

  implicit class RichSiteACL(siteACL: SiteACL) extends Neo4jRep[SiteACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    val hasSite = "HAS_site"
    val hasInstances = "HAS_instances"
    val hasGroups = "HAS_groups"

    override def toNeo4jGraph(siteACL: SiteACL): Node = {

      logger.debug(s"SiteACL Node saved into db - ${siteACL}")
      val map = Map("name" -> siteACL.name)

      val siteACLNode = Neo4jRepository.saveEntity[SiteACL](label, siteACL.id, map)
      val site: Site = null

      //Building relationship to site with SiteACL
      logger.debug(s"SiteACL has relation with Site ${siteACL.site}")
      for (sNode <- siteACL.site) {
        val childNode = site.toNeo4jGraph(sNode)
        Neo4jRepository.createRelation(hasSite, siteACLNode, childNode)
      }

      //Iterating the Instances and linking to the SiteACL
      logger.debug(s"SiteACL has relation with Instance ${siteACL.instances}")
      for (instance <- siteACL.instances) {
        val instanceNode = instance.toNeo4jGraph(instance)
        Neo4jRepository.createRelation(hasInstances, siteACLNode, instanceNode)
      }

      logger.debug(s"SiteACL has relation with UserGroups ${siteACL.groups}")
      for (group <- siteACL.groups) {
        val groupNode = group.toNeo4jGraph(group)
        Neo4jRepository.createRelation(hasGroups, siteACLNode, groupNode)
      }

      siteACLNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SiteACL] = {

        val siteACLNode = Neo4jRepository.findNodeById(nodeId)
        logger.debug(s" SiteACL ${siteACLNode}")

        val siteACLMapOption = Neo4jRepository.getProperties(siteACLNode, "name")
        logger.debug(s" is SiteACL entity ${siteACLMapOption}")

        siteACLMapOption.map( siteACLMap =>{
          val siteNode: List[Node] = Neo4jRepository.getNodesWithRelation(siteACLNode, hasSite)
          val siteList: List[Site] = siteNode.map(child => {
            logger.debug(s" Site -> SiteACL ${child}")
            val site: Site = null
            site.fromNeo4jGraph(child.getId)
          }).flatten

          val site = siteList match {
            case Nil => None
            case (x :: xs) => Some(x)
          }

          val instanceNodes = Neo4jRepository.getNodesWithRelation(siteACLNode, hasInstances)
          val instances = instanceNodes.map(child => {
            logger.debug(s" Instance -> SiteACL ${child}")
            val instance: Instance = null
            instance.fromNeo4jGraph(child.getId)
          }).flatten

          val groupNodes = Neo4jRepository.getNodesWithRelation(siteACLNode, hasGroups)
          val groups = groupNodes.map(child => {
            logger.debug(s"UserGroup -> SiteACL ${child}")
            val group: UserGroup = null
            group.fromNeo4jGraph(child.getId)
          }).flatten

          val userGroup = SiteACL(
            id = Some(siteACLNode.getId),
            name = siteACLMap.get("name").get.asInstanceOf[String],
            site = site,
            instances = instances,
            groups = groups
          )
          logger.debug(s"UserGroup - ${userGroup}")
          userGroup
        })

    }
  }

}
