package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
class AutoScalingPolicy(override val id: Option[Long],
                        application: Application,
                        primaryConditions: List[PolicyCondition],
                        seconderyConditions: List[PolicyCondition],
                        lastAppliedAt: Double) extends BaseEntity