package com.activegrid.model

/**
  * Created by sampathr on 22/9/16.
  */
case class Software(softwareId: Long, createdAt: String, createdBy: String, lastUpdatedAt: String, lastUpdatedBy: String, version: String, name: String, provider: String, downloadURL: String, port: String, processName: String, discoverApplications: String) extends BaseEntity


