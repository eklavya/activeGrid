package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */

sealed trait ScriptType {
  val scriptType: String
}

object ScriptType {

  case object Ruby extends ScriptType {
    override val scriptType: String = "Ruby"
  }

  case object Shell extends ScriptType {
    override val scriptType: String = "Shell"
  }

  case object PuppetDSL extends ScriptType {
    override val scriptType: String = "PuppetDSL"
  }

  case object Jruby extends ScriptType {
    override val scriptType: String = "Jruby"
  }

  case object Ansible extends ScriptType {
    override val scriptType: String = "Ansible"
  }

  case object Yaml extends ScriptType {
    override val scriptType: String = "Yaml"
  }

  case object Play extends ScriptType {
    override val scriptType: String = "Play"
  }

  case object File extends ScriptType {
    override val scriptType: String = "File"
  }

  case object Script extends ScriptType {
    override val scriptType: String = "Script"
  }

  def toScriptType(scriptType: String): ScriptType = {
    scriptType match {
      case "Ruby" => Ruby
      case "Shell" => Shell
      case "PuppetDSL" => PuppetDSL
      case "Jruby" => Jruby
      case "Ansible" => Ansible
      case "Yaml" => Yaml
      case "Play" => Play
      case "File" => File
      case "Script" => Script
    }
  }

  def values: List[ScriptType] = List(Ruby, Shell, PuppetDSL, Jruby, Ansible, Yaml, Play, File, Script)
}