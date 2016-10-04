package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

/**
  * Created by shareefn on 3/10/16.
  */
object Implicits {


  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    override def toGraph(entity: ImageInfo): Option[Node] = {

      val label: String = "ImageInfo"

     val node = GraphDBExecutor.createGraphNode[ImageInfo](entity, label)
      node
    }

    override def fromGraph(nodeId: Long): Option[ImageInfo] = {


      GraphDBExecutor.getEntity[ImageInfo](nodeId)


    }

  }


  implicit class TestImplicitImpl(testImplicit: TestImplicit) extends Neo4jRep[TestImplicit] {
    override def toGraph(entity: TestImplicit): Option[Node] = {


      val label: String = "TestImplicit"

      val mapPrimitives : Map[String, Any] = Map("id" -> entity.id, "name" -> entity.name)

      val node: Option[Node] = GraphDBExecutor.createEmptyGraphNode[TestImplicit](label, mapPrimitives)

      GraphDBExecutor.setGraphProperties(node.get,"set_property_test","success")

      val node2: Option[Node] = entity.image.toGraph(entity.image)


      val relationship = "HAS_image"
      GraphDBExecutor.setGraphRelationship(node,node2,relationship)

      node

    }

    override def fromGraph(nodeId: Long): Option[TestImplicit] = {

      val listOfKeys: List[String] = List("id","name")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val id: Long = propertyValues.get("id").get.toString.toLong
      val name: String = propertyValues.get("name").get.toString

      val relationship = "HAS_image"
      val childNodeId = GraphDBExecutor.getChildNodeId(nodeId,relationship)

      val image:ImageInfo = null
      val imageInfo: Option[ImageInfo] = image.fromGraph(childNodeId)
      println(s"childNodeId = ${childNodeId}")
      println(s"id = ${id}, name = ${name} , imageInfo = ${imageInfo.getOrElse("ab")}")
Some(TestImplicit(id, name ,imageInfo.get))
//      Some(TestImplicit(id,name ,ImageInfo("","","",true,"","","","","","","","","")))
    }




  }

}