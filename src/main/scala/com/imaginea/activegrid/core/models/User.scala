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



