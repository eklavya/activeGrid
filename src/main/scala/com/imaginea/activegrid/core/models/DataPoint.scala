package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 7/12/16.
  */

class DataPoint(timeStamp:Long,value:Double){
  def getValue : Double = value
  def getTimeStamp : Long = timeStamp
}
