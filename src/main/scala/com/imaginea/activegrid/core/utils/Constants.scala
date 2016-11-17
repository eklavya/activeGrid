package com.imaginea.activegrid.core.utils

import java.io.File

import com.typesafe.config.ConfigFactory

/**
  * Created by babjik on 5/10/16.
  */
object Constants {
  val config = ConfigFactory.load

  val tEMPDIRLOC: String = config.getString("tmp.dir")
  val uSERKEYS: String = "userkeys"
  val fILESEPARATOR = File.separator
  val nAMETAGKEY = "Name"

  def getTempDirectoryLocation:String = tEMPDIRLOC
}
