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


/*
private ApplicationTier applicationTier;

    private MetricType metricType;

    private Double threshold;

    private UnitType unitType;

    private ConditionType conditionType;

    private ScaleType scaleType;

    private ScalingGroup scalingGroup;
 */