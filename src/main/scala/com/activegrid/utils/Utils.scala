package com.activegrid.utils

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.Graph
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.factory.{GraphDatabaseFactory, GraphDatabaseSettings}
import org.neo4j.shell.Session

import scala.concurrent.ExecutionContext.Implicits.global;


class Utils {

  def setNodeProperties(n: Node, settings: Map[String, String]) {
    settings.foreach{
      case (k,v) => n.setProperty(k,v);
    }
  }
/*  def javaMapToScalaImutableMap(node:Node): Map[String,String] = {
    val dummyMap   = scala.collection.mutable.HashMap.empty[String,String].empty
    val extraNodeIterator = node.getPropertyKeys.iterator();
    while(extraNodeIterator.hasNext){
      val key = extraNodeIterator.next();
      val value = node.getProperty(key);
      dummyMap += (key->value.asInstanceOf[String])
    }
    dummyMap.toMap;*/
  }

}