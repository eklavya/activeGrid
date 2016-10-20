package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class Filter(override val id: Option[Long]
                  , filterType: FilterType
                  , values: List[String]
                  , condition: Condition) extends BaseEntity

sealed trait FilterType {
  def name: String
  override def toString: String = name
}

case object Tags extends FilterType { val name = "TAGS" }
case object KeypairNames extends FilterType { val name = "KEYPAIR_NAMES" }
case object SecurityGroups extends FilterType { val name = "SECURITY_GROUPS" }
case object IpRanges extends FilterType { val name = "IP_RANGES" }
case object InstanceIds extends FilterType { val name = "INSTANCE_IDS" }
case object Status extends FilterType { val name = "STATUS" }

object FilterType {
  implicit def toFilterType(name: String): FilterType = name match {
    case "TAGS" => Tags
    case "KEYPAIR_NAMES" => KeypairNames
    case "SECURITY_GROUPS" => SecurityGroups
    case "IP_RANGES" => IpRanges
    case "INSTANCE_IDS" => InstanceIds
    case "STATUS" => Status
  }
}

sealed trait Condition {
  def name: String
  override def toString: String = name
}

case object Equals extends Condition { val name = "EQUALS" }
case object Contains extends Condition { val name = "CONTAINS" }
case object StartsWith extends Condition { val name = "STARTSWITH" }
case object EndsWith extends Condition { val name = "ENDSWITH" }

object Condition {
  implicit def toCondition(name: String): Condition = name match {
    case "EQUALS" => Equals
    case "CONTAINS" => Contains
    case "STARTSWITH" => StartsWith
    case "ENDSWITH" => EndsWith
  }
}