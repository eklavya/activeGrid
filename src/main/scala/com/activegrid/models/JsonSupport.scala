package com.activegrid.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}


/**
  * Created by nagulmeeras on 28/09/16.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val settingFormat = jsonFormat2(Setting.apply)

  implicit object DateFormate extends JsonFormat[DateTime] {
    val parser = ISODateTimeFormat.dateOptionalTimeParser()
    override def write(obj: DateTime): JsString = JsString(ISODateTimeFormat.basicDateTime.print(obj))

    override def read(json : JsValue): DateTime = json match {
      case JsString(json) => parser.parseDateTime(json)
      case _ => throw new Exception("Unable to Deserialize")
    }
  }

  implicit val appSettingsFormat = jsonFormat(AppSettings , "id","createdAt","createdBy","lastUpdatedAt","lastUpdatedBy","list")
}
