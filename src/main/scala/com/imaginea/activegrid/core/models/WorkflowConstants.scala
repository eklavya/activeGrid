package com.imaginea.activegrid.core.models
/**
  * Created by sivag on 17/1/17.
  */
object WorkflowConstants {
  val tmpDirLoc = ""
  val maxParlellWorkflows = 10
  val homeDir =  sys.env.get("HOME").getOrElse("/home")
  val defaultPuppetEnv = "production"
  val siteAccessKeyName = "access_key"
  val secretKeyName = "secret_key"
}
