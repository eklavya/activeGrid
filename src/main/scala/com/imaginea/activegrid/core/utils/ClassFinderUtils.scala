package com.imaginea.activegrid.core.utils

import java.io.File

import com.imaginea.activegrid.core.models.Neo4jRepository._
import com.typesafe.scalalogging.Logger
import org.clapper.classutil.{ClassFinder, ClassInfo}
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 29/9/16.
  */
object ClassFinderUtils {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def getClassOfType(clz: Class[_]) : List[ClassInfo] = {
    val baseClass = clz.getName
    logger.debug(s"finding classes which are all extending $baseClass ")
    val classpath = List(".").map(new File(_))
    val finder = ClassFinder(classpath)
    val classes = finder.getClasses
    val classMap = ClassFinder.classInfoMap(classes)
    ClassFinder.concreteSubclasses(baseClass, classMap).toList
  }

}
