package com.activegrid.model

/**
  * Created by sampathr on 22/9/16.
  */
case class Page[T](startIndex:Int, count:Int, totalObjects: Int, objects:List[T]) {
  def this(objects:List[T]) = this(0,objects.size, objects.size, objects)
}


