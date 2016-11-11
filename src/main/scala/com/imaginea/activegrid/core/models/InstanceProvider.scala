/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */
sealed trait InstanceProvider {
  def instanceProvider: String

  override def toString: String = instanceProvider
}

case object AWS extends InstanceProvider {
  override def instanceProvider: String = "AWS"
}

case object OPENSTACK extends InstanceProvider {
  override def instanceProvider: String = "OPENSTACK"
}

case object LAN extends InstanceProvider {
  override def instanceProvider: String = "LAN"
}

case object InstanceProvider {
  def toInstanceProvider(instanceProvider: String): InstanceProvider = {
    instanceProvider match {
      case "AWS" => AWS
      case "OPENSTACK" => OPENSTACK
      case "LAN" => LAN
    }
  }
}
