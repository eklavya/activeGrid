/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 01/11/16.
  */
sealed trait GroupType {
  def groupType: String
}

case object GroupType {

  case object Role extends GroupType {
    override def groupType: String = "role"
  }

  def toGroupType(groupType: String): GroupType = {
    groupType match {
      case "role" => Role
    }
  }
}
