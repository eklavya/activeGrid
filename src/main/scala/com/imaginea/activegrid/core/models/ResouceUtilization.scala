package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 7/12/16.
  */
class ResouceUtilization(target:String,dataPoints:List[DataPoint]) {
  def getDataPoints() : List[DataPoint] = {
    dataPoints
  }
  def getTarget() : String = {
    target
  }
}

object ResouceUtilization {

}
