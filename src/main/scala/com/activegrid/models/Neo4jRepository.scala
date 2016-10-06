//package com.activegrid.models
//
//import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
//import org.neo4j.graphdb.Node
//import org.slf4j.LoggerFactory
//
///**
//  * Created by nagulmeeras on 28/09/16.
//  */
//trait Neo4jRepository extends  {
//
//  //val logger = LoggerFactory.getLogger(getClass)
//
//  def persistEntity[T <: AnyRef](entity: T, labelName: String): Long = withTx {
//    //logger.info(s"Executing $getClass :: persistEntity ")
//    neo => val node: Node = createNode(entity, labelName)(neo)
//      //      entity.getClass.getDeclaredFields.foreach{feild =>
//      //        feild.getType.isInstance(List[T]) match {
//      //          case true =>
//      //          case false => createNode(entity.getClass.getField(feild.getName).get() , labelName)
//      //
//      //
//      //      }}
//     // logger.info("returning from persistEntity method")
//      node.getId
//  }
//
//  def getEntityById[T: Manifest](id: Long, labelName: String): T = withTx {
//    //logger.info(s"Executing $getClass :: getEntity ")
//    neo => val node: Node = getNodeById(id)(neo)
//      node.toCC[T].get
//  }
//
//  def getEntitiesByLabel[T: Manifest](labelName: String): List[T] = withTx {
//    //logger.info(s"Executing $getClass :: getEntitiesByLabel ")
//    neo => val nodes = getAllNodesWithLabel(labelName)(neo)
//      val entities = nodes.map(_.toCC[T].get).toList
//      entities
//  }
//
//  def deleteEntity(labelName: String, propertyName: String, propertyValue: String): Boolean = withTx {
//    //logger.info(s"Executing $getClass :: deleteEntity ")
//    neo => val nodes = findNodesByLabelAndProperty(labelName, propertyName, propertyValue)(neo)
//      nodes.foreach(node => node.delete())
//      true
//  }
//
//  def createEmptyNode(label : String): Node = withTx{
//    neo => val node = createNode(label) (neo)
//      node
//  }
//  def persistRelationShip(parentId: Long, childId: Long, relation: String): Unit = {
//    //val cQl = s"MATCH (user:Users {id: '$parentId'}) FOREACH (id in [$childId] | CREATE (user)-[:$relation]->(:Users {name:name}))"
//  }
//
//  //  def updateEntity[T <: Manifest[T]](labelName : String , propertyName : String , propertyValue : String) : T = withTx{
//  //    neo => val nodes = findNodesByLabelAndProperty(labelName , propertyName , propertyValue)
//  //      entity.getClass.getDeclaredFields.foreach{feild =>
//  //        node.update(feild.getName ,feild.isInstanceOf[feild.type])
//  //
//  //      }
//  //      node.toCC[T].get
//  //  }
//}
