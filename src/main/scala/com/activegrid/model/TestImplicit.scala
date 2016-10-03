package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 3/10/16.
  */


case class TestImplicit(id:Long, name: String, image: ImageInfo) extends BaseEntity

