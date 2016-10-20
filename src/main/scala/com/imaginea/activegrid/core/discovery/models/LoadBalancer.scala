package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class LoadBalancer(override val id: Option[Long]
                        , name: String
                        , vpcId: String
                        , region: String
                        , instanceIds: List[String] = List.empty
                        , availabilityZones: List[String] = List.empty) extends BaseEntity