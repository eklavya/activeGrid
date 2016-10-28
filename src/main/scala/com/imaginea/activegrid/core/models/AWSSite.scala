package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
class AWSSite(override val id: Option[Long],
              name: String,
              instances: Option[List[Instance]],
              filters: Option[List[SiteFilter]],
              keyPairs: Option[List[KeyPairInfo]],
              groupsList: Option[List[Instance]],
              appliacations: Option[List[ApplicationSettings]],
              groupBy: Option[String],
              loadBalancers: List[LoadBalancer],
              scalingGroups: List[ScalingGroup],
              reservedInstanceDetails: List[ReservedInstanceDetails],
              scalingPolicies: List[AutoScalingPolicy]
             ) extends BaseEntity
