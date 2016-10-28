package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
case class Application(name:String,description:String,version:String,instaces:List[Instance],software: Software,tiers:List[ApplicationTiers],aPMServerDetails: APMServerDetails,responseTime:Double)
