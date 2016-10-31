package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 25/10/16.
  */
case class Site1(override val id: Option[Long],
                 siteName: String, instances: List[Instance],
                 filters: List[SiteFilter],
                 loadBalancers: List[LoadBalancer],
                 scalingGroups: List[ScalingGroup]) extends BaseEntity
