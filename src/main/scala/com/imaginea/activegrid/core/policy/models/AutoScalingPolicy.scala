package com.imaginea.activegrid.core.policy.models

import java.util.Date

import com.imaginea.activegrid.core.apm.models.{MetricType, UnitType}
import com.imaginea.activegrid.core.discovery.models.{Application, ApplicationTier, ScalingGroup}
import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class AutoScalingPolicy(override val id: Option[Long]
                             , application: Application
                             , primaryConditions: List[PolicyCondition] = List.empty
                             , secondaryConditions: List[PolicyCondition] = List.empty
                             , lastAppliedAt: Date) extends BaseEntity {
}

case class PolicyCondition(
                            override val id: Option[Long]
                            , applicationTier: ApplicationTier
                            , metricType: MetricType
                            , threshold: Double
                            , unitType: UnitType
                            , conditionType: ConditionType
                            , scaleType: ScaleType
                            , scalingGroup: ScalingGroup) extends BaseEntity


sealed trait ConditionType {
  val name: String

  override def toString: String = name
}

case object GreaterThanCondition extends ConditionType {
  val name = "GREATER_THAN"
}

case object LessThanCondition extends ConditionType {
  val name = "LESSER_THAN"
}

case object EqualToCondition extends ConditionType {
  val name = "EQUAL_TO"
}

object ConditionType {
  implicit def toConditionType(name: String): ConditionType = name match {
    case "GREATER_THAN" => GreaterThanCondition
    case "LESSER_THAN" => LessThanCondition
    case "EQUAL_TO" => EqualToCondition
  }
}