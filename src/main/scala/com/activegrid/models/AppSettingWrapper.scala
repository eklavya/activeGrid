package com.activegrid.models


import com.activegrid.entities.AppSettings
import com.activegrid.neo4j.AppSettingsNeo4jWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by sivag on 26/9/16.
  */
class AppSettingWrapper {

  val appSettingWrapper = new AppSettingsNeo4jWrapper

  def addSettings(appSettings: AppSettings): Future[ExecutionStatus] = Future {
    val executionStatus=new ExecutionStatus
    val maybeNode = appSettingWrapper.toGraph(appSettings)
    maybeNode match {
      case Some(node) =>  executionStatus.status = true
      case _ => executionStatus.status = false
    }
    executionStatus
  }

  def updateSettings(settingsMap: Map[String, String]): Future[ExecutionStatus] = Future {
    appSettingWrapper.updateSettings(settingsMap, appSettingWrapper.lables("HGS").toString)
  }

  def updateAuthSettings(settingsMap: Map[String, String]): Future[ExecutionStatus] = Future {
    appSettingWrapper.updateSettings(settingsMap, appSettingWrapper.lables("HAS").toString)
  }

  def deleteSettings(settingsMap: Map[String, String]): Future[ExecutionStatus] = Future {
    appSettingWrapper.deleteSetting(settingsMap, appSettingWrapper.lables("HGS").toString)
  }

  def deleteAuthSettings(settingsMap: Map[String, String]): Future[ExecutionStatus] = Future {
    appSettingWrapper.deleteSetting(settingsMap, appSettingWrapper.lables("HAS").toString)
  }

  def getSettings(): Future[AppSettings] = Future {
    appSettingWrapper.fromGraph(0L)
  }

}
