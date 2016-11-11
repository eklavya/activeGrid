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
sealed trait ViewType {
  def viewType: String

  override def toString: String = super.toString
}

case object LIST extends ViewType {
  override def viewType: String = "LIST"
}

case object OPERATIONS extends ViewType {
  override def viewType: String = "OPERATIONS"
}

case object ARCHITECTURE extends ViewType {
  override def viewType: String = "ARCHITECTURE"
}

case object ViewType {
  def toViewType(viewType: String): ViewType = {
    viewType match {
      case "LIST" => LIST
      case "OPERATIONS" => OPERATIONS
      case "ARCHITECTURE" => ARCHITECTURE
    }
  }
}

