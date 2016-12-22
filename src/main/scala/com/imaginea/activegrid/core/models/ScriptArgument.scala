package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptArgument(override val id: Option[Long],
                          propName: String,
                          propValue: String,
                          argOrder: Int,
                          nestedArg: ScriptArgument,
                          value: String ) extends BaseEntity
