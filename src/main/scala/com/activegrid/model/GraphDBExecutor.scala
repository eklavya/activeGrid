package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node}

import scala.collection.JavaConversions._


/**
  * Created by shareefn on 23/9/16.
  */
object GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{

  def neo4jStoreDir = "./graphdb/activegrid/test"

  def createGraphNode[T <:BaseEntity: Manifest](entity: T, label:String): Option[Node] =
    withTx { neo =>


      val node = createNode(entity, label)(neo)

      println(s" new node ${node.getLabels}, id ${node.getId}")
      println(s" imageId ${node.getProperty("imageId")}")

      Some(node)
    }


  def persistEntity[T <: BaseEntity: Manifest](entity: T, label: String): Option[T] = {

    withTx { neo =>

      //val node = createNode(entity)(neo)

      val node = createNode(entity, label)(neo)


      println(s" new node ${node.getLabels}, id ${node.getId}")
      println(s" imageId ${node.getProperty("imageId")}")
    }

    return Some(entity)
  }

  def createEmptyGraphNode[T <:BaseEntity: Manifest](label:String, map: Map[String, Any]): Option[Node] =

    withTx { neo =>


      val node = createNode(label)(neo)

      map.foreach { case (k, v) => node.setProperty(k, v) }

      println(s" new node ${node.getLabels}, id ${node.getId}")
      println(s" Id ${node.getProperty("id")}")
      println(s" Name ${node.getProperty("name")}")

      Some(node)
    }



  def setGraphProperties(node:Node, paramName: String, paramValue:Any) =

    withTx { neo =>

      node.setProperty(paramName, paramValue)

    }

  def getGraphProperties(nodeId:Long,listOfKeys: List[String]): Map[String,Any] =

    withTx { neo =>

      val node = getNodeById(nodeId)(neo)
      //val mapOfPrimitives :Map[String,AnyRef]  = node.getAllProperties.asInstanceOf[Map[String,AnyRef]]
      //val keys = node.getPropertyKeys

        listOfKeys.map(key=> (key,node.getProperty(key).asInstanceOf[Any])).toMap[String,Any]


    }

  def setGraphRelationship(fromNode: Option[Node], toNode: Option[Node], relation: String) =

    withTx{ neo=>

      val relType = DynamicRelationshipType.withName(relation)

      fromNode.get --> relType --> toNode.get

     /*start --> relType --> end <
       start.getSingleRelationship(relType, Direction.OUTGOING)*/
    }

  def getChildNodeId(parentNode: Long, relation: String): Long = {

    withTx{ neo=>

    val node = getNodeById(parentNode)(neo)

    node.getSingleRelationship("HAS_image",Direction.OUTGOING).getEndNode.getId

  }
  }





  def getEntities[T:Manifest](label: String): Option[List[T]] = {

    withTx {  neo =>

      println(getAllNodes(neo) )
      val relations = getAllNodesWithLabel(label)(neo).foreach(a=> a.getRelationships)

      println(s"relations ${relations}")

      val nodes = getAllNodesWithLabel(label)(neo)

      Some(nodes.map(_.toCC[T].get).toList)

    }
  }



  def deleteEntity[T<:BaseEntity: Manifest](imageId: Long): Unit = {

    withTx{ neo =>

      val node = getNodeById(imageId)(neo)

      node.delete()

    }
  }



  def getEntity[T<:BaseEntity :Manifest](id: Long): Option[T] = {

    withTx{ neo =>

      val node = getNodeById(id)(neo)

      node.toCC[T]

    }

  }

  def persistEntityTest(entity: Site, label: String): Option[Site] = {

    withTx { neo =>


      /*   val obj : Object = List("1","2").asInstanceOf[Object]

         if(obj.isInstanceOf[Object]) println("obj is instance of Object")*/


      val node = createNode("Site")(neo)

      println(entity.instances)



      val fieldsInSite = entity.getClass.getDeclaredFields()
      fieldsInSite.foreach(println)

      val ClassOfList = classOf[List[String]]
      val ClassOfInt = classOf[Int]
      val ClassOfString = classOf[String]

      fieldsInSite.foreach(field => {


        println("-->" + field.getName + "<---" + field.getType)

        field.setAccessible(true)

        field.getType match {
          case ClassOfList => {


            // val listToArray  = entity.instances.toArray

            val list2Array  = field.get(entity).asInstanceOf[scala.collection.immutable.List[String]].toArray

            node.setProperty(field.getName,list2Array)

          }
          case ClassOfInt  =>{

            node.setProperty(field.getName,field.get(entity).asInstanceOf[Int])

          }

          case ClassOfString => {

            node.setProperty(field.getName,field.get(entity).asInstanceOf[String])
          }
        }


      })


      val m1 = node.getProperties("instances")
      m1 foreach {case (key, value) => {

        println (key + "-->" + value.asInstanceOf[Array[String]](0) + "  " + value.asInstanceOf[Array[String]](1))
      }
      }
      println(node.getProperties("d_id"))
      println(node.getProperties("d_name"))


      println(s" new node ${node.getLabels}, id ${node.getId}")
      //println(s" imageId ${node.getProperty("imageId")}")
    }

    return Some(entity)
  }

  def persistEntityTest[T](entity: List[T], label: String)(implicit x: Neo4jRep[T]): Option[List[T]] = {

//entity.map(test=> x.toGraph(test))

Some(entity)
  }


  def getTest(label: String) : Option[String] = {

    withTx {  neo =>

      val nodes = getAllNodesWithLabel(label)(neo)

      nodes.foreach{ node =>

        println("node properties" + node.getProperties("id","name","set_property_test"))
        println("node rel" + node.getRelationshipTypes)
        println(node.getSingleRelationship("HAS_image",Direction.OUTGOING))

      }
    }
    Some("success")
  }


}
