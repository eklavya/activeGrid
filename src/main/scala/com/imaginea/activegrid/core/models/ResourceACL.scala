package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 26/9/16.
  */
case class ResourceACL (override val id: Option[Long]) extends BaseEntity {
  val resources: String  = ResourceType.All.toString
  val permission: String = Permission.All.toString
  val resourceIds: List[Long] = List.empty[Long]
}
