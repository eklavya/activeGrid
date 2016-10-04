package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.JsonInclude

/**
  * Created by babjik on 26/9/16.
  */
case class User ( username: String
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



