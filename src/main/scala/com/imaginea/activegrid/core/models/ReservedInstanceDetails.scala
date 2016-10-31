package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 31/10/16.
  */
case class ReservedInstanceDetails(override val id: Option[Long],
                                   instanceType: Option[String],
                                   reservedInstancesId: Option[String],
                                   availabilityZone: Option[String],
                                   tenancy: Option[String],
                                   offeringType: Option[String],
                                   productDescription: Option[String],
                                   count: Option[Int]) extends BaseEntity
