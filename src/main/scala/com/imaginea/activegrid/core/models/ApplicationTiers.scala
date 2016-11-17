package com.imaginea.activegrid.core.models

// Created by sivag on 28/10/16.

case class ApplicationTiers(override val id:Option[Long],name:String,descritption:String,
                            instances:List[Instance],aPMServerDetails: APMServerDetails) extends BaseEntity
