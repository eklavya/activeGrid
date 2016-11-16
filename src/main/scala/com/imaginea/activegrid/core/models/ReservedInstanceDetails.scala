package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 31/10/16.
  */
case class ReservedInstanceDetails(override val id: Option[Long],
                                   instanceType: Option[String],
                                   reservedInstancesId: Option[String],
                                   availabilityZone: Option[String],
                                   tenancy: Option[String],
                                   offeringType: Option[String],
                                   productDescription: Option[String],
                                   count: Option[Int]) extends BaseEntity

object ReservedInstanceDetails {
  val reservedInstanceDetailsLabel = "ReservedInstanceDetails"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class ReservedInstanceDetailsImpl(reservedInstanceDetails: ReservedInstanceDetails) extends Neo4jRep[ReservedInstanceDetails] {
    override def toNeo4jGraph(entity: ReservedInstanceDetails): Node = {
      val map = Map("instanceType" -> entity.instanceType,
        "reservedInstancesId" -> entity.reservedInstancesId,
        "availabilityZone" -> entity.availabilityZone,
        "tenancy" -> entity.tenancy,
        "offeringType" -> entity.offeringType,
        "productDescription" -> entity.productDescription,
        "count" -> entity.count)
      Neo4jRepository.saveEntity[ReservedInstanceDetails](reservedInstanceDetailsLabel, entity.id, map)
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ReservedInstanceDetails] = {
      ReservedInstanceDetails.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[ReservedInstanceDetails] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, reservedInstanceDetailsLabel)) {
          val map = Neo4jRepository.getProperties(node, "instanceType", "reservedInstancesId", "availabilityZone",
            "tenancy", "offeringType", "productDescription", "count")

          Some(ReservedInstanceDetails(Some(nodeId),
            ActiveGridUtils.getValueFromMapAs[String](map, "instanceType"),
            ActiveGridUtils.getValueFromMapAs[String](map, "reservedInstancesId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "availabilityZone"),
            ActiveGridUtils.getValueFromMapAs[String](map, "tenancy"),
            ActiveGridUtils.getValueFromMapAs[String](map, "offeringType"),
            ActiveGridUtils.getValueFromMapAs[String](map, "productDescription"),
            ActiveGridUtils.getValueFromMapAs[Int](map, "count")))
        } else {
          None
        }
      case None => None
    }
  }
}
