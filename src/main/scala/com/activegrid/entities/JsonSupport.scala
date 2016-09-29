package com.activegrid.entities

import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsNumber, JsString, JsValue, JsonFormat}

/**
  * Created by nagulmeeras on 28/09/16.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val settingFormat = jsonFormat2(Setting.apply)
  implicit object DateTimeFormate extends JsonFormat[Date]{
    override def write(obj: Date) : JsNumber =  JsNumber(obj.getTime)

    override def read(json: JsValue): Date = json match {
      case JsNumber(date) => new Date()
      case _=> throw new Exception("Unable to Deserialize")
    }
  }
  implicit val appSettingsFormat = jsonFormat6(AppSettings.apply)
}
