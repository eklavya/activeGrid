package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 19/1/17.
  */
abstract  class ExecutionScope(override val id:Option[Long]) extends BaseEntity {
   val scopeType: ScopeType 
}

