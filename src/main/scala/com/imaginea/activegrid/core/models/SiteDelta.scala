package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 18/11/16.
  */
case class SiteDelta(siteId: Option[Long], deltaStatus: SiteDeltaStatus, addedInstances: List[Instance], deletedInstances: List[Instance] )
