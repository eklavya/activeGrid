package com.activegrid.models


import com.activegrid.models.AppSettings.AppSettingsImpl
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 27/09/16.
  */

class AppSettingRepository(implicit executionContext: ExecutionContext) {

  val logger = LoggerFactory.getLogger(getClass)


  def saveAppSettings(appSettings: AppSettings): Future[Option[String]] = Future{
    logger.info(s"Executing $getClass :: saveAppSettings ")
    logger.info("AppSettings : " + appSettings)
    appSettings.toNeo4jGraph()
    Some("Success")
  }

  def getAppSettings(): Future[Option[AppSettings]] = Future {
    logger.info(s"Executing $getClass getAppSettings")
    val appSettings = new AppSettings(Some(0), null, null).fromNeo4jGraph();
    Some(appSettings)
  }


  def saveSetting(setting: Map[String, String]): Future[Option[String]] = Future{
    logger.info(s"Executing $getClass ::saveSetting")
    val appSettingsImpl = new AppSettingsImpl(null)
    val appSettings = appSettingsImpl.updateAppSettings(setting, "Has_Settings")
    Some("Success")
  }

  def getSettings(): Future[Option[AppSettings]] = {
    logger.info(s"Executing $getClass ::getSettings")
    val settings = getAppSettings()
    settings
  }

  def deleteSettings(settingNames: List[String]): Future[Option[String]] = Future {
    logger.info("Executing deleteSettings")
    val appSettingsImpl = new AppSettingsImpl(null)
    appSettingsImpl.deleteSettings(settingNames, "Has_Settings")
    logger.info("Deleted settings")
    Some("Success")
  }


}
