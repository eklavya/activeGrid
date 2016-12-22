package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class Module(override val id: Option[Long],
                  name: String,
                  path: String,
                  version: String,
                  definition: ModuleDefinition) extends BaseEntity
