package com.imaginea.activegrid.core.models

/**
 * Created by babjik on 26/9/16.
 */
object ResourceType extends Enumeration {
  type ResourceType = Value
  val Site, Workflow, Instance, User, UserGroup, All = Value
}
