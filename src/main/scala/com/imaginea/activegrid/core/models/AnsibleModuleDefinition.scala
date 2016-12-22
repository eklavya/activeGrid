package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 21/12/16.
  */
case class AnsibleModuleDefinition(override val id: Option[Long],
                                   playBooks: List[ScriptFile],
                                   roles: List[ScriptFile]) extends ModuleDefinition