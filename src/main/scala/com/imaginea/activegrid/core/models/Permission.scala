package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 12/10/16.
 */

sealed trait Permission {
  val name: String
}

case object CreatePermission extends Permission {
  val name = "Create"
}

case object ReadPermission extends Permission {
  val name = "Read"
}

case object UpdatePermission extends Permission {
  val name = "Update"
}

case object DeletePermission extends Permission {
  val name = "Delete"
}

case object ExecutePermission extends Permission {
  val name = "Execute"
}

case object AllPermission extends Permission {
  val name = "All"
}

object Permission {
  implicit def toPermission(name: String): Permission = name match {
    case "Create" => CreatePermission
    case "Read" => ReadPermission
    case "Update" => UpdatePermission
    case "Delete" => DeletePermission
    case "Execute" => ExecutePermission
    case "All" => AllPermission
  }
}