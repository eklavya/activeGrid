package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
class ScalingGroup(name: String,
                   launchConfigurationName: String,
                   status: String,
                   availabilityZones: List[String] = List.empty,
                   instanceIds: List[String] = List.empty,
                   loadBalancerNames: List[String] = List.empty,
                   tags: List[(String, String)]= List.empty,
                   desiredCapacity: Int,
                   maxCapacity: Int,
                   minCapacity: Int
                    ) extends BaseEntity

