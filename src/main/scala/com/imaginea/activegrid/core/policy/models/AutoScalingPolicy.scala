package com.imaginea.activegrid.core.policy.models

import java.util.Date

import com.imaginea.activegrid.core.apm.models.{UnitType, MetricType}
import com.imaginea.activegrid.core.discovery.models.{ScalingGroup, ApplicationTier, Application}
import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class AutoScalingPolicy(application: Application,
                             primaryConditions: List[PolicyCondition] = List.empty,
                             secondaryConditions: List[PolicyCondition] = List.empty,
                             lastAppliedAt: Date) extends BaseEntity{
}

case class PolicyCondition(applicationTier: ApplicationTier,
                           metricType: MetricType,
                           threshold: Double,
                           unitType: UnitType,
                           conditionType: ConditionType ,
                           scaleType: ScaleType,
                           scalingGroup: ScalingGroup) extends BaseEntity


class ConditionType extends Enumeration {
  type ConditionType = Value
  val GREATER_THAN, LESS_THAN, EQUAL_TO = Value
}
