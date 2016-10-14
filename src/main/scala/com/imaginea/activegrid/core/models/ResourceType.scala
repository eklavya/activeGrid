package com.imaginea.activegrid.core.models

/**
<<<<<<< HEAD
  * Created by babjik on 26/9/16.
  */

sealed trait ResourceType {
  def name: String
  override def toString: String = name
}

case object SiteResource extends ResourceType { val name = "Site" }
case object WorkflowResource extends ResourceType { val name = "Workflow" }
case object InstanceResource extends ResourceType { val name = "Instance" }
case object UserResource extends ResourceType { val name = "User"}
case object UserGroupResource extends ResourceType { val name = "UserGroup"}
case object AllResource extends ResourceType { val name = "All" }

object ResourceType {
  implicit def toResourceType(name: String): ResourceType = name match {
    case "Site" => SiteResource
    case "Workflow" => WorkflowResource
    case "Instance" => InstanceResource
    case "User" => UserResource
    case "UserGroup" => UserGroupResource
    case "All" => AllResource
  }
}
