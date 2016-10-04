package com.activegrid.models

/**
  * Created by nagulmeeras on 04/10/16.
  */
object Action extends Enumeration{
    val GET = Value(0)
    val CREATE = Value(1)
    val UPDATE = Value(2)
    val DELETE = Value(3)
}
