package com.activegrid.model

/**
  * Created by sampathr on 22/9/16.
  */
case class Page[T](startIndex:Int, count:Int, totalObjects: Int, objects:List[Option[T]]) {
}


