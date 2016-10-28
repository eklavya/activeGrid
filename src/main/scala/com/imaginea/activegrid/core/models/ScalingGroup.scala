package com.imaginea.activegrid.core.models


/**
  * Created by sampathr on 28/10/16.
  */
case class ScalingGroup(override val id: Option[Long],
                        name: String,
                        launchConfigurationName: String,
                        status: String,
                        availabilityZones: List[String],
                        instanceIds: List[String],
                        loadBalancerNames: List[String],
                        tags: List[Tuple],
                        desiredCapacity: Long,
                        maxCapacity: Long,
                        minCapacity: Long) extends BaseEntity