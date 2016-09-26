package com.activegrid.services

import scala.concurrent.{ExecutionContext, Future}

import com.activegrid.models.Item

case class ItemService(implicit executionContext: ExecutionContext) {

  def fetchItem(itemId: Long): Future[Option[Item]] = Future(Some(Item("item", itemId)))
}
