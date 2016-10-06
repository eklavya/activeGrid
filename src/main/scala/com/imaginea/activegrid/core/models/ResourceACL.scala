package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 26/9/16.
  */
case class ResourceACL (override val id: Option[Long]) extends BaseEntity/* {
  val resources: String  = ResourceType.All.toString
  val permission: String = Permission.All.toString
  val resourceIds: List[Long] = List.empty[Long]
}
*/

object ResourceACL {

  implicit class RichResourceACL(resourceACL: ResourceACL) extends Neo4jRep[ResourceACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ResourceACL"

    override def toGraph(entity: ResourceACL): Option[Node] = {
      None
    }

    override def fromGraph(nodeId: Long): ResourceACL = {
      ResourceACL(Some(0L))
    }
  }



}