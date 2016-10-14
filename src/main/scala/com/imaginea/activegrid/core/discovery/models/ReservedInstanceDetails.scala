package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class ReservedInstanceDetails(override val id: Option[Long]
                                   , instanceType: String
                                   , reservedInstancesId: String
                                   , availabilityZone: String
                                   , tenancy: String
                                   , offeringType: String
                                   , productDescription: String
                                   , count: Int) extends BaseEntity
