package com.activegrid.repositories

import java.util
import java.util.Date

import akka.http.scaladsl.marshalling.{ToResponseMarshallable, ToResponseMarshaller}
import com.activegrid.entities.{AppSettings, Setting}
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 27/09/16.
  */

class AppSettingRepository(implicit executionContext: ExecutionContext) extends Neo4jRepository {

  override val logger = LoggerFactory.getLogger(getClass)
  val labelName = "AppSettings"

  override def neo4jStoreDir = "./activeGridDB"

  def saveAppSettings(appSettings: AppSettings): Unit = {
    logger.info(s"Executing $getClass :: saveAppSettings ")
    logger.info("AppSettings : " + appSettings)
    val id: Long = persistEntity(appSettings, labelName)
    logger.info(s" saved the app Settings with ID:$id ")
  }

  def getAppSettings(): Future[AppSettings] = Future {
    logger.info(s"Executing $getClass getAppSettings")
    val setting = Setting("Chandu", "Tassu")
    val list: List[Setting] = List()
    list.::(setting)
    logger.info(list.mkString)
    val appSetting: AppSettings = AppSettings(1, new Date(), "Nagu", new Date(), "Meera", list)
    appSetting
  }


  def saveSetting(settings: List[Setting]): Future[Setting] = Future {
    logger.info(s"Executing $getClass ::saveSetting")
    settings.foreach(setting => persistEntity(setting ,labelName))
    settings.head
  }

  def deleteSettings(settingNames: List[Setting]): Unit = {
    logger.info("Executing deleteSettings")
    settingNames.foreach(setting => {
      logger.info(s"  $setting "+setting.key)
      deleteEntity(labelName, "key", setting.key)
    }
    )
    logger.info("Deleted settings")
  }

  def getSettings():Future[List[Setting]]= Future{
    logger.info(s"Executing $getClass ::getSettings")
    val settings = getEntitiesByLabel[Setting](labelName)
    settings
  }

}
