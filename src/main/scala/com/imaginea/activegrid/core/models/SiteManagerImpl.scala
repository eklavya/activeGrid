package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {

  def deleteIntanceFromSite(siteId:Long,instanceId:String) = {
       val site:AWSSite = Neo4jRepository.findNodeById(siteId).asInstanceOf[AWSSite]
       val instance = site.instances.map(l => l.filter(i => (i.id.equals(instanceId))))

       //Removing from Groups list

       site.groupsList.map(l=>l.foreach{i =>
         val group = i.asInstanceOf[InstanceGroup]
         Neo4jRepository.deleteRelation(instanceId.toLong,group,"instances")})


      //Removing from applications

      site.appliacations.map(l=>l.foreach{i =>
      val group = i.asInstanceOf[InstanceGroup]
      Neo4jRepository.deleteRelation(instanceId.toLong,group,"instances")})

      //  Removing from site

       Neo4jRepository.deleteRelation(instanceId.toLong,site,"instances")


  }
}
