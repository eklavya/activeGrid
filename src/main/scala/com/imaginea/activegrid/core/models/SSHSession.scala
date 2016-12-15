package com.imaginea.activegrid.core.models

import com.jcraft.jsch.{JSch, Session}

/**
 * Created by ranjithrajd on 7/12/16.
 */
case class SSHSession(serverIp: String,
                      keyLocation: String,
                      jsch: JSch,
                      session: Option[Session],
                      userName: String,
                      port: Option[Int],
                      passphrase: Option[String],
                      sessionTimeOut: Long
                       )