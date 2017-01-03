package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class InventoryExecutionScope(override val id: Option[Long],
                              json: String,
                              siteId: Long,
                              groups: List[Group],
                              hosts: List[Host],
                              extraVariables: List[Variable]) extends BaseEntity
