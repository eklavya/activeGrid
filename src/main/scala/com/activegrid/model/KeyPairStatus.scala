package com.activegrid.model

/**
  * Created by shareefn on 7/10/16.
  */
object KeyPairStatus extends Enumeration{
  type KeyPairStatus = Value
  val UPLOADED, NOT_YET_UPLOADED, INCORRECT_UPLOAD = Value
}
