package com.activegrid.models

import spray.json.DefaultJsonProtocol

/**
 * Order Model
 */

case class Order(items: List[Item])

/* Separating spray.json helper implicit objects in the OrderJsonProtocol */
object OrderJsonProtocol extends DefaultJsonProtocol {

  import ItemJsonProtocol._ // Order has Item object to parse json

  // Here we are creating json codec automatically.
  implicit val orderFormat = jsonFormat1(Order)
}

