package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
class SiteViewFilter extends  ViewFilter[Site1]{

  val emptyList = List.empty

  def getDetailedSiteView(t: Site1): Site1 ={
    Site1(t.id,t.siteName,t.instances,t.reservedInstanceDetails,t.filters,t.loadBalancers,t.scalingGroups,t.groupsList)
  }

  def getSummarySiteView(t: Site1): Site1 = {
    Site1(t.id,t.siteName,t.instances,t.reservedInstanceDetails,t.filters,t.loadBalancers,t.scalingGroups,t.groupsList)
  }
  override def filterInstance(t: Site1, viewLevel: ViewLevel): Site1 = {
    viewLevel match {
      case DETAILED => getDetailedSiteView(t)
      case SUMMARY => getSummarySiteView(t)
      case _ => getSummarySiteView(t)
    }
  }
}
