package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.imaginea.activegrid.core.models.KeyPairStatus.KeyPairStatus

/**
  * Created by babjik on 26/9/16.
  */
case class KeyPairInfo(override val id: Option[Long]
                      , keyName: String
                      , keyFingerprint: String
                      , keyMaterial: String
                      , filePath: String
                      , status: KeyPairStatus
                      , defaultUser: String
                      , passPhrase: String
                      ) extends BaseEntity