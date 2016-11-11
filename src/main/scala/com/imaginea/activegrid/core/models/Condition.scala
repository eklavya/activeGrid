/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 25/10/16.
  */
sealed trait Condition {
  def condition: String

  override def toString: String = super.toString
}

case object EQUALS extends Condition {
  override def condition: String = "EQUALS"
}

case object CONTAINS extends Condition {
  override def condition: String = "CONTAINS"
}

case object STARTSWITH extends Condition {
  override def condition: String = "STARTSWITH"
}

case object ENDSWITH extends Condition {
  override def condition: String = "ENDSWITH"
}

object Condition {
  def toCondition(name: String): Condition = name match {
    case "EQUALS" => EQUALS
    case "CONTAINS" => CONTAINS
    case "STARTSWITH" => STARTSWITH
    case "ENDSWITH" => ENDSWITH
  }
}
