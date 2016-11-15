package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {

  def deleteIntanceFromSite(siteId:Long,instanceId:String) : ExecutionStatus = {
       val siteNode = Site1.fromNeo4jGraph(siteId)
       val instanceIdLong = instanceId.asInstanceOf[Long]

       if(!siteNode.isDefined){
         ExecutionStatus(false,s"Site ${siteId} not available")
       }
       else {
         val site = siteNode.get
         val instance = site.instances.map(instance => instance.id == instanceIdLong)

         //Removing instance  from groups list
         site.groupsList.map(instanceGroup => Neo4jRepository.deleteRelation(instanceIdLong,instanceGroup,"instances"))

         //Removing from site
         Neo4jRepository.deleteRelation(instanceId.toLong, site, "instances")
         ExecutionStatus(true,s"Instance ${instanceId} deleted from site ${siteId}")
       }


  }
}
