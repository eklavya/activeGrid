/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 25/10/16.
 */

case class SiteACL(override val id: Option[Long]
                   , name: String
                   , site: Option[Site]
                   , instances: List[Instance] = List.empty
                   , groups: List[UserGroup] = List.empty) extends BaseEntity {
}

object SiteACL {
  val label = "SiteACL"

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val hasSite = "HAS_site"
  val hasInstances = "HAS_instances"
  val hasGroups = "HAS_groups"

  implicit class RichSiteACL(siteACL: SiteACL) extends Neo4jRep[SiteACL] {

    override def toNeo4jGraph(siteACL: SiteACL): Node = {

      logger.debug(s"SiteACL Node saved into db - ${siteACL}")
      val map = Map("name" -> siteACL.name)

      val siteACLNode = Neo4jRepository.saveEntity[SiteACL](label, siteACL.id, map)

      //Building relationship to site with SiteACL
      logger.debug(s"SiteACL has relation with Site ${siteACL.site}")
      for (site <- siteACL.site) {
        val childNode = site.toNeo4jGraph(site)
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
      SiteACL.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[SiteACL] = {

    val siteACLNodeOption = Neo4jRepository.findNodeById(nodeId)
    logger.debug(s" SiteACL ${siteACLNodeOption}")
    siteACLNodeOption.map(node => {
      val siteACLMap = Neo4jRepository.getProperties(node, "name")

      logger.debug(s" is SiteACL entity ${siteACLMap}")

      val siteNode: List[Node] = Neo4jRepository.getNodesWithRelation(node, hasSite)
      val siteList: List[Site] = siteNode.map(child => {
        logger.debug(s" Site -> SiteACL ${child}")
        Site.fromNeo4jGraph(child.getId)
      }).flatten

      val site = siteList match {
        case Nil => None
        case (x :: xs) => Some(x)
      }

      val instanceNodes = Neo4jRepository.getNodesWithRelation(node, hasInstances)
      val instances = instanceNodes.flatMap(child => {
        logger.debug(s" Instance -> SiteACL ${child}")
        Instance.fromNeo4jGraph(child.getId)
      })

      val groupNodes = Neo4jRepository.getNodesWithRelation(node, hasGroups)
      val groups = groupNodes.flatMap(child => {
        logger.debug(s"UserGroup -> SiteACL ${child}")
        UserGroup.fromNeo4jGraph(child.getId)
      })

      val siteACL = SiteACL(
        id = Some(node.getId),
        name = siteACLMap("name").toString,
        site = site,
        instances = instances,
        groups = groups
      )
      logger.debug(s"SiteACL - ${siteACL}")
      siteACL
    })
  }

}

