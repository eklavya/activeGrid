package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 09/12/16.
  */
trait Command {
  def execute(commandExecutionContext: CommandExecutionContext, lines: List[Line]): CommandResult
}
