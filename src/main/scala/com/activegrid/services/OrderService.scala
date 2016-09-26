package com.activegrid.services

import scala.concurrent.{ExecutionContext, Future}

import akka.Done

import com.activegrid.models.Order

class OrderService(implicit executionContext: ExecutionContext) {

  // mock async database query api
  def saveOrder(order: Order): Future[Done] = Future(Done)
}
