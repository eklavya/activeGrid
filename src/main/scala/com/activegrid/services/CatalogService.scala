package com.activegrid.services

import com.activegrid.model.{GraphDBExecutor, Software, Test}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by sampathr on 22/9/16.
  */
class CatalogService(implicit val executionContext: ExecutionContext) {

  val db = new GraphDBExecutor()
  val softwareLabel: String = "Softwares"
  val testLabel: String = "Test"

  def buildSoftware(software: Software): Future[Option[Software]] = Future {

    db.persistEntity[Software](software, softwareLabel)

  }


  def deleteSoftware(softwareId: String): Future[Option[String]] = Future {


    val paramName: String = "softwareId"

    db.deleteEntity[Software](softwareLabel, paramName, softwareId)

    Some("true")

  }

  def getSoftwares(): Future[Option[List[Software]]] = Future {

    db.getEntities[Software](softwareLabel)

  }

  def listTestPut(testObj: Test): Future[Option[Test]] = Future {

    db.persistEntity[Test](testObj, testLabel)

  }
}
