package com.imaginea.activegrid.core.models

import com.beust.jcommander.Parameter
import com.imaginea.activegrid.core.utils.CmdExecUtils

import scala.collection.JavaConversions._ // scalastyle:ignore underscore.import

/**
  * Created by shareefn on 16/12/16.
  */
class ChangeDirectoryCommand extends Command {

  @Parameter(description = "the new context to change to")
  var newContext: java.util.List[String] = new java.util.ArrayList()

  override def execute(commandExecutionContext: CommandExecutionContext, lines: List[Line]): CommandResult = {
    val context = newContext.size match {
      case 1 =>
        val targetContext = newContext(0)
        CmdExecUtils.getDervivedContext(targetContext, commandExecutionContext)
      case _ => Some(commandExecutionContext)
    }
    CommandResult(List.empty[Line], context)
  }
}