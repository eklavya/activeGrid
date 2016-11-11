/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 2/11/16.
  */
sealed trait ViewLevel {
  def viewLevel: String

  override def toString: String = super.toString
}

case object SUMMARY extends ViewLevel {
  override def viewLevel: String = "SUMMARY"
}

case object DETAILED extends ViewLevel {
  override def viewLevel: String = "DETAILED"
}

case object ViewLevel {
  def toViewLevel(viewLevel: String): ViewLevel = {
    viewLevel match {
      case "SUMMARY" => SUMMARY
      case "DETAILED" => DETAILED
    }
  }
}


