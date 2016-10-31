package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 31/10/16.
  */
case class ScalingGroup(override val id: Option[Long],
                   name: String,
                   launchConfigurationName: String,
                   status: String,
                   availabilityZones: List[String],
                   instanceIds: List[String],
                   loadBalancerNames: List[String],
                   tags: List[KeyValueInfo],
                   desiredCapacity: Int,
                   maxCapacity: Int,
                   minCapacity: Int) extends BaseEntity

