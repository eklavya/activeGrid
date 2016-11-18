package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 3/11/16.
  */
object SiteManagerImpl {
  
  def deletePolicy(policyId:String): ExecutionStatus ={
      Neo4jRepository.findNodeById(policyId.toLong) match {
        case Some(policyNode) => policyNode.delete()
          ExecutionStatus(true,s"Policy $policyId is deleted successfully")
        case None => ExecutionStatus(false,s"Policy  $policyId not available!!!")
      }
  }
}
