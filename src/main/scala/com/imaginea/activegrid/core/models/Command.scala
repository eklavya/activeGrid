package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 8/12/16.
  */

object Command {
  def executeListCommand(context: CommandExecutionContext, input: List[Line], newContext: List[String]): CommandResult = {
    //TODO
    CommandResult(List.empty[Line], context)
  }
}
