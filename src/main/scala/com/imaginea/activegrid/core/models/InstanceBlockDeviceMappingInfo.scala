package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class InstanceBlockDeviceMappingInfo(override val id:Option[Long],
                                          deviceName : String,
                                          volume : VolumeInfo,
                                          status : String,
                                          attachTime : String,
                                          deleteOnTermination : Boolean,
                                          usage : Int) extends BaseEntity
