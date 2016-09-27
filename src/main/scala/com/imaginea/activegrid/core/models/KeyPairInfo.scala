package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.imaginea.activegrid.core.models.KeyPairStatus.KeyPairStatus

/**
  * Created by babjik on 26/9/16.
  */
case class KeyPairInfo(@JsonInclude(JsonInclude.Include.NON_NULL) keyName: String
                      , @JsonInclude(JsonInclude.Include.NON_NULL) keyFingerprint: String
                      , @JsonInclude(JsonInclude.Include.NON_NULL) keyMaterial: String
                      , @JsonInclude(JsonInclude.Include.NON_NULL) filePath: String
                      , @JsonInclude(JsonInclude.Include.NON_NULL) status: KeyPairStatus
                      , @JsonInclude(JsonInclude.Include.NON_NULL) defaultUser: String
                      , @JsonInclude(JsonInclude.Include.NON_NULL) passPhrase: String

                      ) extends BaseEntity