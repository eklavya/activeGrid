package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 4/1/17.
  */
trait Script extends BaseEntity {
  val name: String
  val description: String
  val language: ScriptType
  val version: String
  val module: Module
  val arguments: List[ScriptArgument]
  val dependencies: PuppetScriptDependencies
}
