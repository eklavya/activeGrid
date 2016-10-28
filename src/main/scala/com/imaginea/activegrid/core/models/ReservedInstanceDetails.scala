package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
case class ReservedInstanceDetails(override val id: Option[Long],
                                   instanceType: String,
                                   reservedInstancesId: String,
                                   availabilityZone: String,
                                   tenancy: String,
                                   offeringType: String,
                                   productDescription: String,
                                   count: Long) extends BaseEntity