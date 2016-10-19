package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 19/10/16.
 */

trait ApplicationException

case class NoDataFound(message: String = "", cause: Throwable = null)
  extends Exception(message, cause) with ApplicationException

case class NodePropertyUnavailable(message: String = "", cause: Throwable = null)
  extends Exception(message, cause) with ApplicationException