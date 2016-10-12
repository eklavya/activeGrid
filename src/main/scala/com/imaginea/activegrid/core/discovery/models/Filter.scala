package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.ResourceType._

/**
 * Created by ranjithrajd on 12/10/16.
 */
class Filter(filterType: FilterType,
              values: List[String],
             condition: Condition) {

}

class FilterType extends Enumeration {
  type FilterType = Value
  val TAGS, KEYPAIR_NAMES, SECURITY_GROUPS, IP_RANGES, INSTANCE_IDS, STATUS = Value
}

class Condition extends Enumeration {
  type Condition = Value
  val EQUALS, CONTAINS, STARTSWITH, ENDSWITH = Value
}
