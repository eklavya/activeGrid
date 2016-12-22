package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptDefinition(override val id: Option[Long],
                            name: String,
                            description: String,
                            language: ScriptType,
                            version: String,
                            module: Module,
                            arguments: List[ScriptArgument],
                            dependencies: PuppetScriptDependencies) extends BaseEntity