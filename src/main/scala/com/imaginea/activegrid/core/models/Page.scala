package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 22/9/16.
  */
case class Page[T] (startIndex: Int, count:Int, totalObjects:Int, objects: List[T])