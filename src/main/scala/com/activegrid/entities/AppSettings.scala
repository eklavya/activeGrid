package com.activegrid.entities


case class AppSettings(settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity


