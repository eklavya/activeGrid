package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 21/12/16.
  */
case class PuppetModuleDefinition(override val id: Option[Long],
                                  files: List[ScriptFile],
                                  templates: List[ScriptFile],
                                  facterFiles: List[ScriptFile],
                                  manifests: List[ScriptFile]) extends ModuleDefinition
