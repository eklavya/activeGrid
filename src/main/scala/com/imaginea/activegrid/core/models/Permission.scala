/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 26/9/16.
  */
sealed trait Permission {
  def name: String

  override def toString: String = name
}

case object Create extends Permission {
  val name = "Create"
}

case object Read extends Permission {
  val name = "Read"
}

case object Update extends Permission {
  val name = "Update"
}

case object Delete extends Permission {
  val name = "Delete"
}

case object Execute extends Permission {
  val name = "Execute"
}

case object All extends Permission {
  val name = "All"
}

object Permission {
  implicit def toPermission(name: String): Permission = name match {
    case "Create" => Create
    case "Read" => Read
    case "Update" => Update
    case "Delete" => Delete
    case "Execute" => Execute
    case "All" => All
  }
}
