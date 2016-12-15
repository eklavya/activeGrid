package com.imaginea.activegrid.core.models

import java.io.Closeable

import com.jcraft.jsch.{ChannelExec, JSch}

/**
 * Created by ranjithrajd on 3/11/16.
 */
case class SSHSession1(serverIp: String,
                      userName: String,
                      keyLocation:String,
                      port: Option[Int],
                      passPhrase: Option[String],
                      sessionTimeout: Int = SSHSession1.defaultSessionTimeout
                      ) extends Closeable {

  val jsch = new JSch()
  val session = jsch.getSession(userName, serverIp)

  def executeCommand(command: String): Option[String]={
    val channelExec: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec]
    channelExec.setPty(true)
    channelExec.setCommand(command)
    getOutputFromChange(channelExec)
  }

  def start(): Unit={
    passPhrase match {
      case Some(passPhrase) => jsch.addIdentity(keyLocation, passPhrase)
      case None => jsch.addIdentity(keyLocation);
    }
    session.setTimeout(sessionTimeout)
    port.foreach( p => session.setPort(p))
    session.connect()
  }

  def getOutputFromChange(chanelExce: ChannelExec): Option[String] = {
    val inputStream = chanelExce.getInputStream()
    chanelExce.connect()
    if (scala.io.Source.fromInputStream(inputStream).isEmpty) {
      None
    } else {
      Some(scala.io.Source.fromInputStream(inputStream).getLines().mkString("\n"))
    }
  }

  override def close(): Unit = {
      this.session.disconnect()
  }
}

object SSHSession1{
  val defaultSessionTimeout = 15000;
}
