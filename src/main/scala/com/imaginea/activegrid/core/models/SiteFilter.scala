package com.imaginea.activegrid.core.models
import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 25/10/16.
  */
case class SiteFilter(override val id : Option[Long],
                      accountInfo: AccountInfo,
                      filters : List[Filter]) extends BaseEntity

object SiteFilter{
  implicit class SiteFilterImpl(siteFilter: SiteFilter) extends Neo4jRep[SiteFilter]{
    override def toNeo4jGraph(entity: SiteFilter): Node = ???

    override def fromNeo4jGraph(nodeId: Long): Option[SiteFilter] = ???
  }
}
