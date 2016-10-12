package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 26/9/16.
  */
object KeyPairStatus extends Enumeration {
  type KeyPairStatus = Value
  val UPLOADED, NOT_YET_UPLOADED, INCORRECT_UPLOAD = Value
}
