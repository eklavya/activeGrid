package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory
import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}


/**
  * Created by sivag on 23/11/16.
  */
case class PolicyCondition(override val id: Option[Long],
                           appTier : Option[ApplicationTier],
                           metricType: Option[MetricType],
                           thresHold : Double,
                           unitType: Option[UnitType],
                           conditionType: Option[ConditionType],
                           scaleType: Option[ScaleType],
                           scalingGroup: Option[ScalingGroup]
                          ) extends BaseEntity
object PolicyCondition {
  val logger = Logger(LoggerFactory.getLogger(PolicyCondition.getClass))

  val lable = PolicyCondition.getClass.getSimpleName
  val relationLable = ActiveGridUtils.relationLbl(lable)

  def fromGraph(id:Long) : Option[PolicyCondition] = {
     Neo.findNodeById(id).map{
       policy =>
         //reading properties
         val properties = Neo.getProperties(policy,"metricType","thresHold","unitType","conditionType","scalingType")

         val metricType = Some(MetricType.toMetricType(properties("metricType").toString))
         val unitType = Some(UnitType.toUnitType(properties("unitType").toString))
         val conditionType = Some(ConditionType.toconditionType(properties("conditionType").toString))
         val scaleType = Some(ScaleType.toScaleType(properties("scalingType").toString))
         val threshold = properties("thresHold").asInstanceOf[Double]


         //Fetching ApplicationTier
         val appTier = Neo.getChildNodeId(policy.getId,ApplicationTier.relationLable).flatMap{
           appTierId => ApplicationTier.fromNeo4jGraph(appTierId)
         }

         //Fetching ScalingGroup
         val scalingGroup = Neo.getChildNodeId(policy.getId,ScalingGroup.relationLable).flatMap{
           sgId => ScalingGroup.fromNeo4jGraph(sgId)
         }

         PolicyCondition(Some(id),appTier,metricType,threshold,unitType,conditionType,scaleType,scalingGroup)

     }
  }


  // Implicit class declaration

  implicit class PolicyConditionImpl(pc: PolicyCondition) extends Neo4jRep[PolicyCondition] {

    override def toNeo4jGraph(entity: PolicyCondition): Node = {
         Neo.withTx{
           neo => {
             val pcNode = Neo.createNode(PolicyCondition.lable)(neo)

             //Setting properties

            pc.metricType.foreach{
              metricType => pcNode.setProperty("metricType",metricType.toString)
            }
             pc.unitType.foreach{
               unitType => pcNode.setProperty("unitType",unitType.toString)
             }
             pc.conditionType.foreach{
               ctype => pcNode.setProperty("conditionType",ctype.toString)
             }
             pc.scaleType.foreach{
               stype => pcNode.setProperty("scalingType",stype.toString)
             }
             pcNode.setProperty("thresHold",pc.thresHold)

             //Setting ApplicationTier

             pc.appTier.map {
               appTier =>
                 val appNode = appTier.toNeo4jGraph(appTier)
                 Neo.createRelation(ApplicationTier.relationLable,pcNode,appNode)
             }

             pc.scalingGroup.map{
               sgroup =>
                 val sGroupNode = sgroup.toNeo4jGraph(sgroup)
                 Neo.createRelation(ScalingGroup.relationLable,pcNode,sGroupNode)
             }
             pcNode
           }
         }
    }

    override def fromNeo4jGraph(id: Long): Option[PolicyCondition] = {
       PolicyCondition.fromGraph(id)
    }

  }

}
