package com.activegrid.models


import com.activegrid.models.AppSettings.AppSettingsImpl
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 27/09/16.
  */

class AppSettingRepository(implicit executionContext: ExecutionContext) {

  val logger = LoggerFactory.getLogger(getClass)


  def saveAppSettings(appSettings: AppSettings): Future[Unit] = Future {
    logger.info(s"Executing $getClass :: saveAppSettings ")
    logger.info("AppSettings : " + appSettings)
    appSettings.toNeo4jGraph()
  }

  def getAppSettings(): Future[AppSettings] = Future {
    logger.info(s"Executing $getClass getAppSettings")
    val nodeId = AppSettings.getAppSettingNode().getId
    val appSettings = AppSettings.fromNeo4jGraph(nodeId)
    if (appSettings == null)
      throw new Exception("Unable to find App Settings")
    else
      appSettings
  }


  def saveSetting(setting: Map[String, String]): Future[Unit] = Future {
    logger.info(s"Executing $getClass ::saveSetting")
    val appSettingsImpl = new AppSettingsImpl(AppSettings(Some(0), Map.empty, Map.empty))
    try {
      val appSettings = appSettingsImpl.updateAppSettings(setting, "Has_Settings")
    } catch {
      case exception: Exception => throw exception
    }
  }

  def getSettings(): Future[AppSettings] = {
    logger.info(s"Executing $getClass ::getSettings")
    val settings = getAppSettings()
    settings
  }

  def deleteSettings(settingNames: List[String]): Future[Boolean] = Future {
    logger.info("Executing deleteSettings")
    val appSettingsImpl = new AppSettingsImpl(null)
    try {
      appSettingsImpl.deleteSettings(settingNames, "Has_Settings")
      logger.info("Deleted settings")
    } catch {
      case exception: Exception => throw exception
    }
    true
  }


}
