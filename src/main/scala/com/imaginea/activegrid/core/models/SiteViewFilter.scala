package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
class SiteViewFilter extends  ViewFilter[AWSSite]{

  val emptyList = List.empty

  def getDetailedSiteView(t: AWSSite): AWSSite ={
    AWSSite(Some(0L),t.name,t.instances,Some(List.empty[SiteFilter]),Some(List.empty[KeyPairInfo]),Some(List.empty[Instance]),Some(List.empty[Application]),Some(""),List.empty[LoadBalancer],List.empty[ScalingGroup],List.empty[ReservedInstanceDetails],List.empty[AutoScalingPolicy])
  }

  def getSummarySiteView(t: AWSSite): AWSSite = {
    AWSSite(Some(0L),t.name,Some(List.empty[Instance]),Some(List.empty[SiteFilter]),Some(List.empty[KeyPairInfo]),Some(List.empty[Instance]),Some(List.empty[Application]),Some(""),List.empty[LoadBalancer],List.empty[ScalingGroup],List.empty[ReservedInstanceDetails],List.empty[AutoScalingPolicy])
  }
  override def filterInstance(t: AWSSite, viewLevel: ViewLevel): AWSSite = {
    viewLevel match {
      case DETAILED => getDetailedSiteView(t)
      case SUMMARY => getSummarySiteView(t)
      case _ => getSummarySiteView(t)
    }
  }
}
