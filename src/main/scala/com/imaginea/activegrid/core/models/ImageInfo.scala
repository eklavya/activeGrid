package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonIgnoreProperties, JsonInclude, JsonProperty}

/**
  * Created by babjik on 22/9/16.
  */

@JsonIgnoreProperties(ignoreUnknown = true)
case class ImageInfo ( override val id: Option[Long]
                      , imageId: String
                      ,  state: String
                      ,  ownerId: String
                      ,  publicValue: Boolean
                      ,  architecture: String
                      ,  imageType: String
                      ,  platform: String
                      ,  imageOwnerAlias: String
                      ,  name: String
                      ,  description: String
                      ,  rootDeviceType: String
                      ,  rootDeviceName: String
                      ,  version: String) extends BaseEntity
