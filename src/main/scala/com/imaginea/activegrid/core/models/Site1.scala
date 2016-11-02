package com.imaginea.activegrid.core.models
import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 25/10/16.
  */

case class Site1(override val id : Option[Long],
                 siteName: String,
                 instances: List[Instance],
                 filters:List[SiteFilter]) extends BaseEntity

object Site1{
  val repository = Neo4jRepository
  val site1Label = "Site1"
  val site_Instance_Relation = "HAS_INSTANCE"
  val site_Filter_Relation = "HAS_FILTER"
  implicit class Site1Impl(site1: Site1) extends Neo4jRep[Site1]{
    override def toNeo4jGraph(entity: Site1): Node = {
      repository.withTx{
        neo =>
          val node = repository.createNode(site1Label)(neo)
          if(entity.siteName.nonEmpty) node.setProperty("siteName" , entity.siteName)
          if(entity.instances.nonEmpty){
            entity.instances.foreach{
              instance =>
                val childNode = instance.toNeo4jGraph(instance)
                repository.createRelation(site_Instance_Relation , node , childNode)
            }
          }
          if(entity.filters.nonEmpty){
            entity.filters.foreach{
              filter =>
                val childNode = filter.toNeo4jGraph(filter)
                repository.createRelation(site_Filter_Relation , node , childNode)
            }
          }
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Site1] = ???
  }
}






