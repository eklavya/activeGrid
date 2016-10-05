package com.activegrid.model

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}

/**
  * Created by shareefn on 22/9/16.
  */
@JsonIgnoreProperties(ignoreUnknown = true)
case class ImageInfo(override val id: Option[Long],
                     @JsonInclude(JsonInclude.Include.NON_NULL) state : String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) ownerId  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) publicValue :Boolean,
                     @JsonInclude(JsonInclude.Include.NON_NULL) architecture  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) imageType  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) platform  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) imageOwnerAlias  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) name  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) description  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) rootDeviceType  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) rootDeviceName  :String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) version :String) extends BaseEntity