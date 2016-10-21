package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 22/9/16.
  */
case class Page[T](startIndex: Int, count: Int, totalObjects: Int, objects: List[T])

object Page {
  def apply[T](objects: List[T]): Page[T] = Page[T](0, objects.size, objects.size, objects)
}
