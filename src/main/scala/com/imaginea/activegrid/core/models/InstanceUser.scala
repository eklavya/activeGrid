package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 12/10/16.
 */
class InstanceUser(override val id: Option[Long]
                   , userName: String
                   , publicKeys: List[String]) extends BaseEntity
