package com.imaginea.activegrid.core.models

import java.util.Date


/**
  * Created by shareefn on 24/11/16.
  */
case class TerminalSession(id: Long,
                           user: Option[String],
                           commandLineHistory: List[String],
                           currentCmdExecContext: CommandExecutionContext,
                           sshSessions: List[SSHSession],
                           startedAt: Date,
                           lastUsedAt: Date)
