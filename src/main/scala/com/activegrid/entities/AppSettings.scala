package com.activegrid.entities

import spray.json.DefaultJsonProtocol._


case class AppSettings(settings:Map[String,String],authSettings:Map[String,String]) extends BaseEntity


