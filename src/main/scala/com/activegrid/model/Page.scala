package com.activegrid.model

/**
  * Created by shareefn on 26/9/16.
  */

case class Page[T](startIndex:Int, count:Int, totalObjects: Int, objects:List[T])


