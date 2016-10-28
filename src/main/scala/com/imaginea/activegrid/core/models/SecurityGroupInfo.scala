package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class SecurityGroupInfo(override val id: Option[Long],
                             groupName: String,
                             groupId: String,
                             ownerId: String,
                             description: String,
                             ipPermissions: List[IpPermissionInfo],
                             tags: List[KeyValueInfo]) extends BaseEntity
