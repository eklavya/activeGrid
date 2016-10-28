package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
case class LoadBalancer(override val id:Option[Long],name:String,vpcId:String,region:String,instanceIds:Option[List[String]],availabilityZones:Option[List[String]]) extends BaseEntity
