package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 19/1/17.
  */
case class ExecutionScope(override val id:Option[Long],scopeType: ScopeType) extends BaseEntity(id:Option[Long])


