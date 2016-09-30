package com.activegrid.models

import java.util.Date

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 27/09/16.
  */

class AppSettingRepository(implicit executionContext: ExecutionContext) extends Neo4jRepository {

  override val logger = LoggerFactory.getLogger(getClass)
  val settingsLabelName = "Settings"
  val appSettingsLabelName = "AppSettings"

  override def neo4jStoreDir = "./activeGridDB"

  def saveAppSettings(appSettings: AppSettings): Future[AppSettings] = Future {
    logger.info(s"Executing $getClass :: saveAppSettings ")
    logger.info("AppSettings : " + appSettings)
    val id: Long = persistEntity(appSettings, appSettingsLabelName)
    logger.info(s" saved the app Settings with ID:$id ")
    appSettings
  }

  def getAppSettings(): Future[List[AppSettings]] = Future {
    logger.info(s"Executing $getClass getAppSettings")
    val appSettings = getEntitiesByLabel(appSettingsLabelName)
    appSettings
  }


  def saveSetting(settings: List[Setting]): Future[Setting] = Future {
    logger.info(s"Executing $getClass ::saveSetting")
    settings.foreach(setting => persistEntity(setting, settingsLabelName))
    settings.head
  }

  def getSettings(): Future[List[Setting]] = Future {
    logger.info(s"Executing $getClass ::getSettings")
    val settings = getEntitiesByLabel[Setting](settingsLabelName)
    settings
  }

  def deleteSettings(settingNames: List[Setting]): Unit = {
    logger.info("Executing deleteSettings")
    settingNames.foreach(setting => {
      logger.info(s"  $setting " + setting.key)
      deleteEntity(settingsLabelName, "key", setting.key)
    }
    )
    logger.info("Deleted settings")
  }


}
