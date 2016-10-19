package com.activegrid.model

/**
  * Created by shareefn on 7/10/16.
  */
//object KeyPairStatus extends Enumeration{
//  type KeyPairStatus = Value
//  val UPLOADED, NOT_YET_UPLOADED, INCORRECT_UPLOAD = Value
//}


sealed trait KeyPairStatus {
  def value: String

  override def toString: String = value
}

case object UPLOADED extends KeyPairStatus {
  val value = "UPLOADED"
}

case object NOT_YET_UPLOADED extends KeyPairStatus {
  val value = "NOT_YET_UPLOADED"
}

case object INCORRECT_UPLOAD extends KeyPairStatus {
  val value = "INCORRECT_UPLOAD"
}

object KeyPairStatus {
  implicit def toKeyPairStatus(value: String): KeyPairStatus = value match {
    case "UPLOADED" => UPLOADED
    case "NOT_YET_UPLOADED" => NOT_YET_UPLOADED
    case "INCORRECT_UPLOAD" => INCORRECT_UPLOAD
  }
}