package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class Host(override val id: Option[Long],
                instanceId: String,
                variables: List[Variable]) extends BaseEntity
