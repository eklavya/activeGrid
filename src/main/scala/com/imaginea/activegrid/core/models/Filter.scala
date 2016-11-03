package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 25/10/16.
  */
case class Filter(override val id: Option[Long], filterType: FilterType, values: List[String], condition: Option[Condition]) extends BaseEntity
