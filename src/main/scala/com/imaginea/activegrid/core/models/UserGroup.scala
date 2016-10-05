package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 5/10/16.
  */
case class UserGroup(override val id: Option[Long]
                    , name: String
                    , users: Option[Set[User]]
                    , accesses: Option[Set[ResourceACL]]) extends BaseEntity
