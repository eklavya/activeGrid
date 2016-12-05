package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
trait ViewFilter[T <: BaseEntity] {
  def filterInstance(t: T, viewLevel: ViewLevel): T
}

