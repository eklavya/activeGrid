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
