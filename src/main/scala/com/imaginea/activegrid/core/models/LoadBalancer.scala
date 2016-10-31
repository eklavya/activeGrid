package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 31/10/16.
  */
case class LoadBalancer(override val id: Option[Long],
                        name: String,
                        vpcId: String,
                        region: Option[String],
                        instanceId: List[String],
                        availabilityZones: List[String]) extends BaseEntity
