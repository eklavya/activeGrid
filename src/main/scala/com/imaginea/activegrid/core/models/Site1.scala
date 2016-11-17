package com.imaginea.activegrid.core.models

import java.util.concurrent.{Executors, TimeUnit}

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 25/10/16.
  */
case class Site1(override val id: Option[Long],
                 siteName: String,
                 instances: List[Instance],
                 reservedInstanceDetails: List[ReservedInstanceDetails],
                 filters: List[SiteFilter],
                 loadBalancers: List[LoadBalancer],
                 scalingGroups: List[ScalingGroup],
                 groupsList: List[InstanceGroup]) extends BaseEntity

object Site1 {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "Site1"
  val siteLBRelation = "HAS_LoadBalancer"
  val siteSGRelation = "HAS_ScalingGroup"
  val siteIGRelation = "HAS_InstanceGroup"
  val siteRIRelation = "HAS_ReservedInstance"
  val siteSFRelation = "HAS_SiteFilter"

  def delete(siteId: Long): ExecutionStatus = {
    val maybeNode = Neo4jRepository.findNodeById(siteId)
    maybeNode match {
      case Some(node) => {
        logger.info(s"Site is ${siteId} available,It properties are...." + node.toString)
        Neo4jRepository.deleteEntity(node.getId)
        ExecutionStatus(true,"Site deleted successfully")
      }
      case None =>
        ExecutionStatus(false,"Site not available")
    }
  }

  def apply(id : Long):Site1 = {
    Site1(Some(id),"test",List.empty[Instance],List.empty[ReservedInstanceDetails],
      List.empty[SiteFilter],List.empty[LoadBalancer],List.empty[ScalingGroup],List.empty[InstanceGroup])

  }
  def fromNeo4jGraph(nodeId: Long): Option[Site1] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "siteName")
        val siteName = map.get("siteName").toString
        val relationshipinst = "HAS_Instance"
        val childNodeIdsInst: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationshipinst)
        val instances: List[Instance] = childNodeIdsInst.flatMap { childId =>
          Instance.fromNeo4jGraph(childId)
        }
        /*val relationship_sf = "HAS_SiteFilter"
        val childNodeIds_sf: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_sf)
        val siteFilters: List[SiteFilter] = childNodeIds_sf.flatMap { childId =>
          SiteFilter.fromNeo4jGraph(childId)
        }*/
        val childNodeIdsSf: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteSFRelation)
        val siteFilters: List[SiteFilter] = childNodeIdsSf.flatMap { childId =>
          SiteFilter.fromNeo4jGraph(childId)
        }

        val childNodeIdsLb: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteLBRelation)
        val loadBalancers: List[LoadBalancer] = childNodeIdsLb.flatMap { childId =>
          LoadBalancer.fromNeo4jGraph(childId)
        }

        val childNodeIdsSg: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteSGRelation)
        val scalingGroups: List[ScalingGroup] = childNodeIdsSg.flatMap { childId =>
          ScalingGroup.fromNeo4jGraph(childId)
        }

        val childNodeIdsIg: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteIGRelation)
        val instanceGroups: List[InstanceGroup] = childNodeIdsIg.flatMap { childId =>
          InstanceGroup.fromNeo4jGraph(childId)
        }

        val childNodeIdsRid: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteRIRelation)
        val reservedInstance: List[ReservedInstanceDetails] = childNodeIdsRid.flatMap { childId =>
          ReservedInstanceDetails.fromNeo4jGraph(childId)
        }
        //logger.info(s"Printing : ${instances.head.blockDeviceMappings}")
        Some(Site1(Some(nodeId), siteName, instances, reservedInstance, siteFilters, loadBalancers, scalingGroups, instanceGroups))
      case None =>
        logger.warn(s"could not find node for Site with nodeId $nodeId")
        None
    }
  }

  // scalastyle:off
  implicit class RichSite1(site1: Site1) extends Neo4jRep[Site1] {

    override def toNeo4jGraph(entity: Site1): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jgraph of site")
      val label = "Site1"
      val mapPrimitives = Map("siteName" -> entity.siteName)
      val node = Neo4jRepository.saveEntity[Site1](label, entity.id, mapPrimitives)
      logger.info("After saving site primitive types ")
      val relationship_inst = "HAS_Instance"

//      entity.instances.foreach { instance =>
//        logger.info("Executing instance")
//         new FutureTask[String](new Callable[String] {
//           override def call(): String = {
//
//             "Done"
//           }
//         })
//      }
      val executorService = Executors.newFixedThreadPool(10)
      entity.instances.foreach{
        instance =>
          executorService.submit(new Runnable {
            override def run(): Unit = {
              val instanceNode = instance.toNeo4jGraph(instance)
              Neo4jRepository.setGraphRelationship(node, instanceNode, relationship_inst)
            }
          })
      }
      executorService.awaitTermination(5 , TimeUnit.SECONDS)
      executorService.shutdown()

      entity.filters.foreach { filter =>
        val filterNode = filter.toNeo4jGraph(filter)
        Neo4jRepository.setGraphRelationship(node, filterNode, siteSFRelation)
      }

      entity.loadBalancers.foreach { lb =>
        val loadBalancerNode = lb.toNeo4jGraph(lb)
        Neo4jRepository.setGraphRelationship(node, loadBalancerNode, siteLBRelation)
      }
      entity.scalingGroups.foreach { sg =>
        val scalingGroupNode = sg.toNeo4jGraph(sg)
        Neo4jRepository.setGraphRelationship(node, scalingGroupNode, siteSGRelation)
      }

      entity.reservedInstanceDetails.foreach { ri =>
        val reservedInstanceNode = ri.toNeo4jGraph(ri)
        Neo4jRepository.setGraphRelationship(node, reservedInstanceNode, siteRIRelation)
      }

      entity.groupsList.foreach { ig =>
        val instanceGroupNode = ig.toNeo4jGraph(ig)
        Neo4jRepository.setGraphRelationship(node, instanceGroupNode, siteIGRelation)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site1] = {
      Site1.fromNeo4jGraph(id)
    }
  }

}

class InstanceSaver(instance: Instance) extends Runnable{
  override def run(): Unit = {
    instance.toNeo4jGraph(instance)
  }
}
