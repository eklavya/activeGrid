package com.imaginea.activegrid.core.utils

import akka.actor.ActorSystem
import com.amazonaws.regions.RegionUtils
import com.imaginea.Main
import com.imaginea.activegrid.core.models._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext // scalastyle:ignore underscore.import

/**
  * Created by babjik on 13/10/16.
  */
object ActiveGridUtils {

  def getUriInfo(): String = {
    "http" + "://" + HOST + ":" + PORT + "/"
  }

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val config = ConfigFactory.load
  //scalastyle:off field.name
  val HOST = config.getString("http.host")
  val PORT = config.getInt("http.port")
  val DBPATH = config.getString("neo4j.dbpath")
  val APIVERSION = config.getString("http.version")
  //scalastyle:on field.name

  def getValueFromMapAs[T](map: Map[String, Any], key: String): Option[T] = {
    map.get(key).map(_.asInstanceOf[T])
  }

  def convertToBooleanValue(value: Any): Boolean = {
    value match {
      case Some(booleanVal) => booleanVal.asInstanceOf[Boolean]
      case _ => false
    }
  }

  def getRegions(instanceProvider: InstanceProvider): List[String] = {
    RegionUtils.getRegions.foldLeft(List.empty[String]) {
      (list, region) =>
        region.getName :: list
    }
  }

  def relationLbl(clsName: String): String = {
    "HAS_" + clsName
  }

  def getProperty[T: Manifest](propertyMap: Map[String, JsValue], property: String): Option[T] = {
    if (propertyMap.contains(property)) {
      propertyMap(property) match {
        case JsString(str) => Some(str.asInstanceOf[T])
        case JsNumber(str) => Some(str.asInstanceOf[T])
        case JsFalse => Some(false.asInstanceOf[T])
        case JsTrue => Some(true.asInstanceOf[T])
        case JsObject(str) => Some(str.asInstanceOf[T])
        case _ => None
      }
    } else {
      None
    }
  }

  def getObjectsFromJson[T: Manifest](propertyMap: Map[String, JsValue], property: String, formateObject: RootJsonFormat[T]): List[T] = {
    if (propertyMap.contains(property)) {
      val listOfObjs = propertyMap(property).asInstanceOf[JsArray]
      listOfObjs.elements.toList.map(formateObject.read)
    } else {
      List.empty[T]
    }
  }

  def objectToJsValue[T](fieldName: String, obj: Option[T], jsonFormat: RootJsonFormat[T], rest: List[JsField] = Nil): List[(String, JsValue)] = {
    obj match {
      case Some(x) => (fieldName, jsonFormat.write(x.asInstanceOf[T])) :: rest
      case None => rest
    }
  }

  def listToJsValue[T](fieldName: String, objList: List[T], jsonFormat: RootJsonFormat[T], rest: List[JsField] = Nil): List[(String, JsValue)] = {
    objList.map { obj => (fieldName, jsonFormat.write(obj))
    }
  }

  def setToJsValue[T](fieldName: String, objList: Set[T], jsonFormat: RootJsonFormat[T], rest: List[JsField] = Nil): List[(String, JsValue)] = {
    objList.map { obj => (fieldName, jsonFormat.write(obj))
    }.toList
  }

  def longToJsField(fieldName: String, fieldValue: Option[Long], rest: List[JsField] = Nil): List[(String, JsValue)] = {
    fieldValue match {
      case Some(x) => (fieldName, JsNumber(x)) :: rest
      case None => rest
    }
  }

  def stringToJsField(fieldName: String, fieldValue: Option[String], rest: List[JsField] = Nil): List[(String, JsValue)] = {
    fieldValue match {
      case Some(x) => (fieldName, JsString(x)) :: rest
      case None => rest
    }
  }

  /**
    *
    * @param serviceName
    * @return
    *         New or Existing ExecutionContext mapped to serviceName
    */
  def getExecutionContextByService(serviceName:String) : ExecutionContext = {
    serviceName match  {
      case "POLICY" => Main.system.dispatcher
      // todo implementation
      case "WORKFLOW" => Main.system.dispatcher
      // todo implementation
      case _ => Main.system.dispatcher
    }
  }
}
