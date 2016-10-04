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
    //val id: Long = persistEntity(appSettings, appSettingsLabelName)
    //logger.info(s" saved the app Settings with ID:$id ")
    appSettings
  }

  def getAppSettings(): Future[AppSettings] = Future {
    logger.info(s"Executing $getClass getAppSettings")
    val appSettingsImpl = new AppSettingsImpl(null)
    val appSettings1 : AppSettings =  appSettingsImpl.fromGraph()
    appSettings1
  }


  def saveSetting(setting :Setting): Future[Setting] = Future {
    logger.info(s"Executing $getClass ::saveSetting")
    val appSettingsImpl = new AppSettingsImpl(null)
    val settingsImpl = new SettingsImpl(setting)
    val settingNode = settingsImpl.toGraph()
    val appSettingsNode = appSettingsImpl.getAppSettingNode()
    Implicits.createRelationShip(appSettingsNode,settingNode,"Has")
    setting
  }

//  def getSettings(): Future[List[Setting]] = Future {
//    logger.info(s"Executing $getClass ::getSettings")
//    val settings = getEntitiesByLabel[Setting](settingsLabelName)
//    settings
//  }

//  def deleteSettings(settingNames: List[Setting]): Unit = {
//    logger.info("Executing deleteSettings")
//    settingNames.foreach(setting => {
//      logger.info(s"  $setting " + setting.key)
//      deleteEntity(settingsLabelName, "key", setting.key)
//    }
//    )
//    logger.info("Deleted settings")
//  }


}
