package com.activegrid.models


import com.activegrid.entities.AppSettings
import com.activegrid.neo4j.AppSettingsNeo4jWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by sivag on 26/9/16.
  */
class AppSettingImpl {

  val appSettingWrapper = new AppSettingsNeo4jWrapper;


  def addSettings(appSettings: AppSettings): Future[String] = Future {
    val node = appSettingWrapper.toGraph(appSettings)
    node match {
      case Some(node) => "success"
      case _ => "fail"
    }
  }

  def updateSettings(settingsMap: Map[String, String]): Future[String] = Future {
    appSettingWrapper.updateSettings(settingsMap, appSettingWrapper.lables.get("HGS").toString)
  }

  def updateAuthSettings(settingsMap: Map[String, String]): Future[String] = Future {
    appSettingWrapper.updateSettings(settingsMap, appSettingWrapper.lables.get("HAS").toString)
  }

  def deleteSettings(settingsMap: Map[String, String]): Future[String] = Future {
    appSettingWrapper.deleteSetting(settingsMap, appSettingWrapper.lables.get("HGS").toString)
  }

  def deleteAuthSettings(settingsMap: Map[String, String]): Future[String] = Future {
    appSettingWrapper.deleteSetting(settingsMap, appSettingWrapper.lables.get("HAS").toString)
  }

  def getSettings(): Future[AppSettings] = Future {
    appSettingWrapper.fromGraph(0L)
  }

}
