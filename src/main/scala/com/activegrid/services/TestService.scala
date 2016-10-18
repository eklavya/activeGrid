package com.activegrid.services

import com.activegrid.model._

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by shareefn on 14/10/16.
  */
class TestService(implicit val executionContext: ExecutionContext) {

  def saveTestAll(entity: Instance): Future[Option[String]] = Future{

    entity.toNeo4jGraph(entity)

    Some("success")
  }

  def getTest(number: Int): Future[Instance] = Future {
    val label: String = "TestImplicit"

    //  val success = GraphDBExecutor.getTest(label)

    Instance.fromNeo4jGraph(Some(number)).get

  }

}
