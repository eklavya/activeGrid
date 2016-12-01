package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
object SiteViewFilter extends ViewFilter[Site1] {

  override def filterInstance(t: Site1, viewLevel: ViewLevel): Site1 = {
    viewLevel match {
      case DETAILED => getDetailedSiteView(t)
      case SUMMARY => getSummarySiteView(t)
      case _ => getSummarySiteView(t)
    }
  }

  def getDetailedSiteView(t: Site1): Site1 = {
    Site1(t.id, t.siteName, t.instances, t.reservedInstanceDetails, t.filters, t.loadBalancers, t.scalingGroups, t.groupsList,t.applications, t.groupBy )
  }

  def getSummarySiteView(t: Site1): Site1 = {
    Site1(t.id, t.siteName, t.instances, List.empty[ReservedInstanceDetails],
      List.empty[SiteFilter], List.empty[LoadBalancer], List.empty[ScalingGroup], List.empty[InstanceGroup],List.empty[Application], t.groupBy)
  }
}