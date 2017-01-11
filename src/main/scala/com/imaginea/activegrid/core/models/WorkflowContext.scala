package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 11/1/17.
  */
class WorkflowContext {

}
object WorkflowContext {
  def get() : WorkflowContext = {
    new WorkflowContext()
  }
}
