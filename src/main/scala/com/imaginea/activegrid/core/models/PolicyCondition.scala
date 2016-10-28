package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
case class PolicyCondition(override val id: Option[Long],
                           applicationTier: ApplicationTier,
                           metricType: MetricType,
                           threshold: Double,
                           unitType: UnitType,
                           conditionType: ConditionType,
                           scaleType: ScaleType,
                           scalingGroup: ScalingGroup) extends BaseEntity