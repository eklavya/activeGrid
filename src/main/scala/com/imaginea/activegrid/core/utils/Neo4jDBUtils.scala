package com.imaginea.activegrid.core.utils

import java.util.ServiceLoader

import com.imaginea.activegrid.core.models.BaseEntity

/**
  * Created by babjik on 29/9/16.
  */
object Neo4jDBUtils {

  val EDGE_LABEL_PREFIX = "HAS_"

  def getDefaultEdgeLabel(label: String): String = s"$EDGE_LABEL_PREFIX$label"

}
