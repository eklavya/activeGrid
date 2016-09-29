package com.activegrid.model

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import scala.collection.JavaConversions._


/**
  * Created by shareefn on 23/9/16.
  */
class GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{

  def neo4jStoreDir = "./graphdb/activegrid"

  def persistEntity[T <: BaseEntity: Manifest](entity: T, label: String): Option[T] = {

    withTx { neo =>

      //val node = createNode(entity)(neo)

      val node = createNode(entity, label)(neo)
      println(s" new node ${node.getLabels}, id ${node.getId}")
      println(s" imageId ${node.getProperty("imageId")}")
    }

    return Some(entity)
  }

  def getEntities[T:Manifest]: Option[List[T]] = {

    withTx {  neo =>

      val nodes = getAllNodes(neo)

      Some(nodes.map(_.toCC[T].get).toList)

    }
  }



  def deleteEntity[T<:BaseEntity: Manifest](label: String, paramName: String, paramValue:Any): Unit = {

    withTx{ neo =>

      val nodes = findNodesByLabelAndProperty(label,paramName,paramValue)(neo)
      nodes.foreach(println)
      nodes.map(_.delete())

    }
  }


  def getEntity[T<:BaseEntity :Manifest](siteId: Long): Option[T] = {

    withTx{ neo =>

      val node = getNodeById(siteId)(neo)

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


}
