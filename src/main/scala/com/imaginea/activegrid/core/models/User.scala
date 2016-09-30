package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.JsonInclude

/**
  * Created by babjik on 26/9/16.
  */
case class User (@JsonInclude(JsonInclude.Include.NON_NULL) username: String
                ,@JsonInclude(JsonInclude.Include.NON_NULL) password: String
                ,@JsonInclude(JsonInclude.Include.NON_NULL) email: String
                ,@JsonInclude(JsonInclude.Include.NON_NULL) uniqueId: String
                ,@JsonInclude(JsonInclude.Include.NON_NULL) publicKeys: List[KeyPairInfo]
                ,@JsonInclude(JsonInclude.Include.NON_NULL) accountNonExpired: Boolean
                ,@JsonInclude(JsonInclude.Include.NON_NULL) accountNonLocked: Boolean
                ,@JsonInclude(JsonInclude.Include.NON_NULL) credentialsNonExpired: Boolean
                ,@JsonInclude(JsonInclude.Include.NON_NULL) enabled: Boolean
                ,@JsonInclude(JsonInclude.Include.NON_NULL) displayName: String) extends BaseEntity
