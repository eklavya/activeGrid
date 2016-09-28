package com.activegrid.models

import spray.json.DefaultJsonProtocol

/**
 * Item Model
 */

case class Item(name: String, id: Long)

/* Separating spray.json helper implicit objects in the ItemJsonProtocol */
object ItemJsonProtocol extends DefaultJsonProtocol {

  // Here we are creating json codec automatically.
  implicit val itemFormat = jsonFormat2(Item)
}