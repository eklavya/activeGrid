package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 24/11/16.
  */
case class CommandExecutionContext(contextName : String,
                                   contextType: ContextType,
                                   contextObject : Site1,
                                   parentContext: Option[CommandExecutionContext],
                                   instances : Array[String],
                                   siteId: Long,
                                   user: String)
