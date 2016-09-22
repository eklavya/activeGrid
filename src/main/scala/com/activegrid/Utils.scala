package com.activegrid

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global;


class Utils {

  def persistSoftwareDetails(software: Software): Future[Software] = Future {
    Software("1","Ubuntu","Linux")
  }

}