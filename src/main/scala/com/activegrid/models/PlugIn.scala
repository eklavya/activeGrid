package com.activegrid.models

/**
  * Created by nagulmeeras on 18/10/16.
  */
case class PlugIn(override val id: Option[Long],
                  name: String,
                  active: Boolean,
                  classLoader: Option[ClassLoader]) extends BaseEntity

