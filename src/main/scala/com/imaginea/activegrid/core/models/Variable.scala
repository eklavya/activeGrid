package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class Variable(override val id: Option[Long],
                    name: String,
                    value: String,
                    scope: VariableScope,
                    readOnly: Boolean,
                    hidden: Boolean) extends BaseEntity
