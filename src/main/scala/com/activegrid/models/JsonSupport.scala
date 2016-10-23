package com.activegrid.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}


/**
  * Created by nagulmeeras on 28/09/16.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val appSettingsFormat = jsonFormat(AppSettings.apply, "id", "settings", "authSettings")

}
