package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 24/11/16.
  */
case class CommandExecutionContext(contextName: String,
                                   contextType: ContextType,
                                   contextObject: Option[BaseEntity],
                                   parentContext: Option[CommandExecutionContext],
                                   instances: Array[String],
                                   siteId: Long,
                                   user: Option[String])

object CommandExecutionContext {
  def apply(contextName: String,
            contextType: ContextType,
            contextObject: Option[BaseEntity],
            parentContext: Option[CommandExecutionContext]
           ): CommandExecutionContext = CommandExecutionContext(contextName, contextType, contextObject, parentContext, Array.empty[String], 1, None)
}