package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class IpPermissionInfo(override val id : Option[Long],
                            fromPort : Int,
                            toPort : Int,
                            ipProtocol : IpProtocol,
                            groupIds : Set[String],
                            ipRanges : List[String])extends BaseEntity
