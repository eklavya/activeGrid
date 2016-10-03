package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 3/10/16.
  */
object Implicits {


  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    override def toGraph(entity: ImageInfo): Option[Node] = {

      val label: String = "ImageInfo"

      val node = GraphDBExecutor.createGraphNode[ImageInfo](entity, label)

      Some(node)
    }

  }


  implicit class TestImplicitImpl(testImplicit: TestImplicit) extends Neo4jRep[TestImplicit] {
    override def toGraph(entity: TestImplicit): Option[Node] = {


      val label: String = "TestImplicit"

      val mapPrimitives : Map[String, Any] = Map("id" -> testImplicit.id, "name" -> testImplicit.name)

      val node = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

      val node2 = entity.image.toGraph(entity.image)

      node -> "HAS_image" -> node2


      Some(node)

    }


  }

}