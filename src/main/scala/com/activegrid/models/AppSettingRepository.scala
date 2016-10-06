package com.activegrid.models


import com.activegrid.models.Implicits.{AppSettingsImpl, SettingsImpl}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 27/09/16.
  */

class AppSettingRepository(implicit executionContext: ExecutionContext)  {

  val logger = LoggerFactory.getLogger(getClass)
  val settingsLabelName = "Settings"
  val appSettingsLabelName = "AppSettings"


  def saveAppSettings(appSettings: AppSettings): Future[AppSettings] = Future {
    logger.info(s"Executing $getClass :: saveAppSettings ")
    logger.info("AppSettings : " + appSettings)
    val appSettingImpl = new AppSettingsImpl(appSettings)
    appSettingImpl.toGraph()
    appSettings
  }

  def getAppSettings(): Future[AppSettings] = Future {
    logger.info(s"Executing $getClass getAppSettings")
    val appSettingsImpl = new AppSettingsImpl(null)
    val appSettings : AppSettings =  appSettingsImpl.fromGraph()
    appSettings
  }


  def saveSetting(setting :Map[String,String]): Unit ={
    logger.info(s"Executing $getClass ::saveSetting")
    val appSettingsImpl = new AppSettingsImpl(null)
    val appSettings = appSettingsImpl.updateAppSettings(setting,"Has_Settings")

  }

  def getSettings(): Future[AppSettings] ={
    logger.info(s"Executing $getClass ::getSettings")
    val settings = getAppSettings()
    settings
  }

  def deleteSettings(settingNames: List[String]): Unit = {
    logger.info("Executing deleteSettings")
    val appSettingsImpl = new AppSettingsImpl(null)
    appSettingsImpl.deleteSettings(settingNames, "Has_Settings")
    logger.info("Deleted settings")
  }


}
