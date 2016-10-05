package com.activegrid.model


import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}

/**
  * Created by sampathr on 22/9/16.
  */
@JsonIgnoreProperties(ignoreUnknown = true)
case class Software(override val id: Option[Long],
                     @JsonInclude(JsonInclude.Include.NON_NULL) version: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) name: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) provider: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) downloadURL: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) port: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) processNames: Array[String],
                     @JsonInclude(JsonInclude.Include.NON_NULL) discoverApplications: Boolean) extends BaseEntity