package com.activegrid.utils

import org.neo4j.graphdb.Node;


class Utils {

  def setNodeProperties(n: Node, settings: Map[String, String]) {
    settings.foreach {
      case (k, v) => n.setProperty(k, v);
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
      dummyMap.toMap;
}*/

}