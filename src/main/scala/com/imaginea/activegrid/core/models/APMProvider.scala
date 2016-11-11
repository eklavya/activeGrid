/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 14/10/16.
  */
sealed trait APMProvider {
  def provider: String

  override def toString: String = provider
}

case object GRAPHITE extends APMProvider {
  val provider = "GRAPHITE"
}

case object NEWRELIC extends APMProvider {
  val provider = "NEWRELIC"
}

case object APPDYNAMICS extends APMProvider {
  val provider = "APPDYNAMICS"
}

case object APMProvider {
  def toProvider(provider: String): APMProvider = {
    provider match {
      case "GRAPHITE" => GRAPHITE
      case "NEWRELIC" => NEWRELIC
      case "APPDYNAMICS" => APPDYNAMICS
    }
  }
}
