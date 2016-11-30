package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.{Neo4jRepository => Neo}
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory


/**
  * Created by sivag on 24/11/16.
  */
case class AutoScalingPolicy(override val id: Option[Long], application: Option[Application],
                             primaryConditions: List[PolicyCondition],
                             secondaryConditions: List[PolicyCondition],
                             lastAppliedAt: String) extends BaseEntity

object AutoScalingPolicy {

  val logger = LoggerFactory.getLogger(AutoScalingPolicy.getClass)
  val lable = AutoScalingPolicy.getClass.getSimpleName

  val conditionLbl2 = "HAS_SECONDARY_COND"
  val conditionLbl1 = "HAS_PRIMARY_COND"

  def fromNeo4jGraph(id: Long): Option[AutoScalingPolicy] = {

    Neo.findNodeByLabelAndId(AutoScalingPolicy.lable, id).map {
      policy =>
        val primeConditions = Neo.getChildNodeIds(policy.getId, conditionLbl1).flatMap {
          id => PolicyCondition.fromGraph(id)
        }
        //Fetching Secondary Primary Coditions
        val secondConditions = Neo.getChildNodeIds(policy.getId, conditionLbl2).flatMap {
          id => PolicyCondition.fromGraph(id)
        }
        //Fetching applications
        val application = Neo.getChildNodeIds(policy.getId, Application.relationLabel).flatMap {
          id => Application.fromNeo4jGraph(id)
        }.headOption

        val lastAppliedAt = if (policy.hasProperty("appliedTime")) {
          policy.getProperties("appliedTime").toString
        } else {
          ""
        }

        //AutoScalingPolicy(Some(id),primeConditions,secondConditins,application,lastAppliedAt)
        AutoScalingPolicy(Some(id), application, primeConditions, secondConditions, lastAppliedAt)

    }
  }

  implicit class AutoScalingPolicyImpl(autoScalingPolicy: AutoScalingPolicy) extends Neo4jRep[AutoScalingPolicy] {

    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    override def toNeo4jGraph(entity: AutoScalingPolicy): Node = {

      val properties = Map("appliedTime" -> entity.lastAppliedAt);
      val policyNode = Neo.saveEntity[AutoScalingPolicy](AutoScalingPolicy.lable, entity.id, properties)

      //Mapping applications
      val appNode = entity.application.foreach {
        app =>
          val appNode = app.toNeo4jGraph(app)
          Neo.createRelation(Application.relationLabel, policyNode, appNode)
      }

      //Mapping primary conditions
      entity.primaryConditions.foreach {
        policy =>
          val conditionNode = policy.toNeo4jGraph(policy)
          Neo.createRelation(AutoScalingPolicy.conditionLbl1, policyNode, conditionNode)
      }
      //Mapping secondaryconditions
      entity.secondaryConditions.foreach {
        policy =>
          val conditionNode = policy.toNeo4jGraph(policy)
          Neo.createRelation(AutoScalingPolicy.conditionLbl2, policyNode, conditionNode)
      }
      policyNode
    }

    override def fromNeo4jGraph(id: Long): Option[AutoScalingPolicy] = {
      AutoScalingPolicy.fromNeo4jGraph(id)
    }
  }

}
