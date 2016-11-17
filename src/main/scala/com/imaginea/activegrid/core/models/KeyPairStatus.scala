package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 26/9/16.
  */
sealed trait KeyPairStatus {
  def name: String

  override def toString: String = name
}

case object UploadedKeyPair extends KeyPairStatus {
  val name = "UPLOADED"
}

case object NotYetUploadedKeyPair extends KeyPairStatus {
  val name = "NOT_YET_UPLOADED"
}

case object IncorrectUploadKeyPair extends KeyPairStatus {
  val name = "INCORRECT_UPLOAD"
}

object KeyPairStatus {
  def toKeyPairStatus(name: String): KeyPairStatus = name match {
    case "UPLOADED" => UploadedKeyPair
    case "NOT_YET_UPLOADED" => NotYetUploadedKeyPair
    case "INCORRECT_UPLOAD" => IncorrectUploadKeyPair
  }
}
