package com.activegrid.utils

import org.neo4j.graphdb.Node


class Utils {

  def setNodeProperties(n: Node, settings: Map[String, String]) {
    settings.foreach {
      case (k, v) => n.setProperty(k, v);
    }
  }
}