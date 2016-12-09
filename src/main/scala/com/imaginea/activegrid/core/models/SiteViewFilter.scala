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
    Site1(t.id, t.siteName, t.instances, t.reservedInstanceDetails, t.filters,
      t.loadBalancers, t.scalingGroups, t.groupsList, t.applications,
      t.groupBy, t.scalingPolicies)
  }

  def getSummarySiteView(t: Site1): Site1 = {
    Site1(t.id, t.siteName, t.instances, List.empty[ReservedInstanceDetails],
      List.empty[SiteFilter], List.empty[LoadBalancer], List.empty[ScalingGroup],
      List.empty[InstanceGroup], List.empty[Application], t.groupBy, t.scalingPolicies)
  }

  def fetchSite(site1: Site1, viewLevel: ViewLevel): List[String] = {
    viewLevel.viewLevel match {
      case SUMMARY.viewLevel => List(site1.siteName)
      case DETAILED.viewLevel =>
        site1.id match {
          case Some(id) => List(site1.siteName, id.toString)
          case None => List(site1.siteName)
        }
    }
  }
}
