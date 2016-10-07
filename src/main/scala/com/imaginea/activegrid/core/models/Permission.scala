package com.imaginea.activegrid.core.models

/**
 * Created by babjik on 26/9/16.
 */
object Permission extends Enumeration {
  type Permission = Value
  val Create, Read, Update, Delete, Execute, All = Value
}
