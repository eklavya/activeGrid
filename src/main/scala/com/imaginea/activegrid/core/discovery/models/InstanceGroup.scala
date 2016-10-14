package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
class InstanceGroup(groupType: GroupType,
                    name: String,
                    instances: List[Instance] = List.empty) extends BaseEntity

sealed trait GroupType{
  def name: String
  override def toString: String = name
}
case object RoleGroupType extends GroupType { val name = "ROLE" }

object GroupType{
  implicit def toGroupType(name: String): GroupType = name match {
    case "ROLE" => RoleGroupType
  }
}

