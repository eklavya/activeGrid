package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 28/10/16.
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
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val repository = Neo4jRepository
  val reservedInstanceDetailsLabel = "ReservedInstanceDetails"

  implicit class ReservedInstanceDetailsImpl(reservedInstanceDetails: ReservedInstanceDetails) extends Neo4jRep[ReservedInstanceDetails] {
    override def toNeo4jGraph(entity: ReservedInstanceDetails): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(reservedInstanceDetailsLabel)(neo)
          if (entity.instanceType.nonEmpty) node.setProperty("instanceType", entity.instanceType.get)
          if (entity.reservedInstancesId.nonEmpty) node.setProperty("reservedInstancesId", entity.reservedInstancesId.get)
          if (entity.availabilityZone.nonEmpty) node.setProperty("availabilityZone", entity.availabilityZone.get)
          if (entity.tenancy.nonEmpty) node.setProperty("tenancy", entity.tenancy.get)
          if (entity.offeringType.nonEmpty) node.setProperty("offeringType", entity.offeringType.get)
          if (entity.productDescription.nonEmpty) node.setProperty("productDescription", entity.productDescription.get)
          if (entity.count.nonEmpty) node.setProperty("count", entity.count.get)
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ReservedInstanceDetails] = {
      ReservedInstanceDetails.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[ReservedInstanceDetails] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        if (repository.hasLabel(node, reservedInstanceDetailsLabel)) {
          Some(ReservedInstanceDetails(Some(nodeId),
            repository.getProperty[String](node, "instanceType"),
            repository.getProperty[String](node, "reservedInstancesId"),
            repository.getProperty[String](node, "availabilityZone"),
            repository.getProperty[String](node, "tenancy"),
            repository.getProperty[String](node, "offeringType"),
            repository.getProperty[String](node, "productDescription"),
            repository.getProperty[Int](node, "count")))
        } else {
          logger.debug("No Reserved Intsances Found")
          None
        }
    }
  }
}