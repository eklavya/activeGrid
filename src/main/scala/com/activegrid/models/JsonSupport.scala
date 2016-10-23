package com.activegrid.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}


/**
  * Created by nagulmeeras on 28/09/16.
  */
trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  val logger = LoggerFactory.getLogger(getClass)
  implicit val appSettingsFormat = jsonFormat(AppSettings.apply, "id", "settings", "authSettings")
  implicit val siteFormat = jsonFormat(Site.apply,"id","siteName","groupBy")

  implicit object apmProviderFormat extends RootJsonFormat[APMProvider]{
    override def write(obj: APMProvider): JsValue = {
      logger.info(s"Writing APMProvider json : ${obj.provider.toString}")
      JsString(obj.provider.toString)
    }

    override def read(json: JsValue): APMProvider = {
      logger.info(s"Reading json value : ${json.toString}")
      json match {
        case JsString(str) => APMProvider.toProvider(str)
        case _=> throw new DeserializationException("Unable to deserialize the Provider data")
      }
    }
  }

  implicit val apmServerDetailsFormat = jsonFormat(APMServerDetails.apply , "id","name","serverUrl","monitoredSite","provider","headers")




}
