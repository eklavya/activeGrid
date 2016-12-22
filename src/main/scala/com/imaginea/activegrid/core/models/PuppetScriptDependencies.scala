package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class PuppetScriptDependencies(override val id: Option[Long],
                                    files: List[ScriptFile],
                                    templates: List[ScriptFile],
                                    facterFiles: List[ScriptFile]) extends BaseEntity