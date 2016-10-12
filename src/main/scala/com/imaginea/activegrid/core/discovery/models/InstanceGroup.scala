package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.BaseEntity

/**
 * Created by ranjithrajd on 12/10/16.
 */
class InstanceGroup(groupType: GroupType,
                    name: String,
                    instances: List[Instance] = List.empty) extends BaseEntity

class GroupType extends Enumeration {
  type GroupType = Value
  val ROLE = Value
}