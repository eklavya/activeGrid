package com.activegrid.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


/**
  * Created by nagulmeeras on 28/09/16.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val appSettingsFormat = jsonFormat(AppSettings.apply, "id", "settings", "authSettings")
}
