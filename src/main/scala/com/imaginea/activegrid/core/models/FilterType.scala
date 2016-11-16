package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */
sealed trait FilterType {
  def filterType: String

  override def toString: String = filterType
}

case object TAGS extends FilterType {
  override def filterType: String = "TAGS"
}

case object KEYPAIRNAMES extends FilterType {
  override def filterType: String = "KEYPAIR_NAMES"
}

case object SECURITYGROUPS extends FilterType {
  override def filterType: String = "SECURITY_GROUPS"
}

case object IPRANGES extends FilterType {
  override def filterType: String = "IP_RANGES"
}

case object INSTANCEIDS extends FilterType {
  override def filterType: String = "INSTANCE_IDS"
}

case object STATUS extends FilterType {
  override def filterType: String = "STATUS"
}

case object UNKNOWN extends FilterType {
  override def filterType: String = "UNKNOWN"
}

case object FilterType {
  def toFilteType(filterType: String): FilterType = {
    filterType match {
      case "TAGS" => TAGS
      case "KEYPAIR_NAMES" => KEYPAIRNAMES
      case "SECURITY_GROUPS" => SECURITYGROUPS
      case "IP_RANGES" => IPRANGES
      case "INSTANCE_IDS" => INSTANCEIDS
      case "STATUS" => STATUS
      case _=> UNKNOWN
    }
  }
}
