package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 20/10/16.
  */
case class ExecutionStatus(status:Boolean,msg:String)

object ExecutionStatus{

  def apply(status: Boolean): ExecutionStatus = new ExecutionStatus(status, "")

 def getMsg(es:ExecutionStatus): String ={
     return es.msg
 }
}