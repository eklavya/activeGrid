package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 4/1/17.
  */
trait Script extends BaseEntity {
  val name: Option[String]
  val description: Option[String]
  val language: ScriptType
  val version: Option[String]
  val module: Option[Module]
  val arguments: List[ScriptArgument]
  val dependencies: Option[PuppetScriptDependencies]
}
