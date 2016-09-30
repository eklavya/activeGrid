package com.activegrid.model

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}


/**
  * Created by sampathr on 22/9/16.
  */
class GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {

  def neo4jStoreDir = "./graphdb/activegrid"

  def persistEntity[T <: BaseEntity : Manifest](entity: T, label: String): Option[T] = {

    withTx { neo =>

      //val node = createNode(entity)(neo)

      val node = createNode(entity, label)(neo)
      //      println(s" new software ${node.getLabels}, id ${node.getId}")
      //      println(s" Software Name ${node.getProperty("name")}")
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
            //TO DO: need to get entity.instances in other way
            // val listToArray = entity.instances.toArray
            val list2Array = field.get(entity).asInstanceOf[scala.collection.immutable.List[String]].toArray
            node.setProperty(field.getName,list2Array)
          }
          case ClassOfInt =>{
            node.setProperty(field.getName,field.get(entity).asInstanceOf[Int])
          }
          case ClassOfString => {
            node.setProperty(field.getName,field.get(entity).asInstanceOf[String])
          }
        }
      })
      println("Test Run")
    }


    return Some(entity)
  }


  def getEntities[T: Manifest](label: String): Option[List[T]] = {

    withTx { neo =>

      val nodes = getAllNodesWithLabel(label)(neo)
      println(s"  nodes $nodes")
      Some(nodes.map(_.toCC[T].get).toList)

    }
  }


  def deleteEntity[T <: BaseEntity : Manifest](label: String, paramName: String, paramValue: Any): Unit = {

    withTx { neo =>

      val nodes = findNodesByLabelAndProperty(label, paramName, paramValue)(neo)
      nodes.foreach(println)
      nodes.map(_.delete())

    }
  }

}
