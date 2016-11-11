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
sealed trait ResourceType {
  def name: String

  override def toString: String = name
}

case object SiteResourceType extends ResourceType {
  val name = "Site"
}

case object WorkflowResourceType extends ResourceType {
  val name = "Workflow"
}

case object InstanceResourceType extends ResourceType {
  val name = "Instance"
}

case object UserResourceType extends ResourceType {
  val name = "User"
}

case object UserGroupResourceType extends ResourceType {
  val name = "UserGroup"
}

case object ResourceTypeAll extends ResourceType {
  val name = "All"
}

object ResourceType {
  implicit def toResourceType(name: String): ResourceType = name match {
    case "Site" => SiteResourceType
    case "Workflow" => WorkflowResourceType
    case "Instance" => InstanceResourceType
    case "User" => UserResourceType
    case "UserGroup" => UserGroupResourceType
    case "All" => ResourceTypeAll
  }
}
