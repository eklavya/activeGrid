package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class Group(override val id: Option[Long],
                 name: String,
                 instanceIds: List[String],
                 variables: List[Variable]) extends BaseEntity
