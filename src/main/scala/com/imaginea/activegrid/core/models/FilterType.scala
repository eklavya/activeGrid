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
sealed trait FilterType {
  def filterType: String

  override def toString: String = super.toString
}

case object TAGS extends FilterType {
  override def filterType: String = "TAGS"
}

case object KEYPAIR_NAMES extends FilterType {
  override def filterType: String = "KEYPAIR_NAMES"
}

case object SECURITY_GROUPS extends FilterType {
  override def filterType: String = "SECURITY_GROUPS"
}

case object IP_RANGES extends FilterType {
  override def filterType: String = "IP_RANGES"
}

case object INSTANCE_IDS extends FilterType {
  override def filterType: String = "INSTANCE_IDS"
}

case object STATUS extends FilterType {
  override def filterType: String = "STATUS"
}

case object FilterType {
  def toFilteType(filterType: String): FilterType = {
    filterType match {
      case "TAGS" => TAGS
      case "KEYPAIR_NAMES" => KEYPAIR_NAMES
      case "SECURITY_GROUPS" => SECURITY_GROUPS
      case "IP_RANGES" => IP_RANGES
      case "INSTANCE_IDS" => INSTANCE_IDS
      case "STATUS" => STATUS
    }
  }
}
