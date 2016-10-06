package com.imaginea.activegrid.core.models

import spray.json.DefaultJsonProtocol

/**
  * Created by babjik on 5/10/16.
  */
case class UserGroup(id: Option[Long]
                    , name: String
                    , users: Option[Set[User]]
                    , accesses: Option[Set[ResourceACL]]) extends BaseEntity

case class UserGroupProxy(name: String) extends BaseEntity

object UserGroupProtocol{
  val labelUserGroup = "UserGroup"

 /*implicit val userGroupResourceFormat = jsonFormat3(ResourceACL)
 implicit val userGroupFormat = jsonFormat4(UserGroup)
 implicit val pageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")*/
}
