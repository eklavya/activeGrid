package com.imaginea.activegrid.core.models

import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol._

/**
  * Created by babjik on 26/9/16.
  */
case class User (id: Option[Long]
                , username: String
                , password: String
                , email: String
                , uniqueId: String
                , publicKeys: List[KeyPairInfo]
                , accountNonExpired: Boolean
                , accountNonLocked: Boolean
                , credentialsNonExpired: Boolean
                , enabled: Boolean
                , displayName: String)
  extends BaseEntity

case class UserGroup(id: Option[Long]
                ,users: Set[User]
                ,name: String
                ,access: Set[ResourceACL])
  extends BaseEntity

case class UserGroupProxy(name: String) extends BaseEntity

object UserGroupProtocol extends DefaultJsonProtocol{
  val labelUserGroup = "UserGroup"

 /* implicit val userGroupResourceFormat = jsonFormat3(ResourceACL)
  implicit val userGroupFormat = jsonFormat4(UserGroup)
  implicit val pageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")*/
}


