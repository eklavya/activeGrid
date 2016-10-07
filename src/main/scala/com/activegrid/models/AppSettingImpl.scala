package com.activegrid.models


import com.activegrid.Neo4j.AppSettingsNeo4jWrapper
import com.activegrid.entities.{AppSettings}
import com.activegrid.utils.Utils
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.neo4j.driver.v1._
import org.neo4j.graphdb.{Direction, Node, Relationship}
import org.neo4j.kernel.api.Neo4jTypes.RelationshipType
import org.neo4j.kernel.impl.api.store.RelationshipIterator.Empty

import spray.json.DefaultJsonProtocol._
import scala.collection.mutable


/**
  * Created by sivag on 26/9/16.
  */
class AppSettingImpl {

  val appSettingWrapper = new AppSettingsNeo4jWrapper;


  def addSettings(appSettings: AppSettings): Future[String] = Future {
        appSettingWrapper.toGraph(appSettings)
        "success"
  }

  def updateSettings(settingsMap:Map[String,String]): Future[String] = Future {
       appSettingWrapper.updateSettings(settingsMap,appSettingWrapper.lables.get("HGS").toString)
  }
  def updateAuthSettings(settingsMap:Map[String,String]): Future[String] = Future {
    appSettingWrapper.updateSettings(settingsMap,appSettingWrapper.lables.get("HAS").toString)
  }
  def deleteSettings(settingsMap:Map[String,String]): Future[String] = Future {
       appSettingWrapper.deleteSetting(settingsMap,appSettingWrapper.lables.get("HGS").toString)
  }
  def deleteAuthSettings(settingsMap:Map[String,String]): Future[String] = Future {
    appSettingWrapper.deleteSetting(settingsMap,appSettingWrapper.lables.get("HAS").toString)
  }

  def getSettings(): Future[AppSettings] = Future {
    appSettingWrapper.fromGraph(0L)
  }

}
