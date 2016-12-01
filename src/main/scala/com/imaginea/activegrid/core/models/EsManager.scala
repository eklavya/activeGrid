package com.imaginea.activegrid.core.models

import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.node.NodeBuilder
import spray.json.RootJsonFormat

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 23/11/16.
  */
object EsManager {
  val settings = ImmutableSettings.settingsBuilder()
    .put("cluster.name", "elasticsearch")
    .put("node.name", this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath + "/config/names.txt")
    .build()
  val node = NodeBuilder.nodeBuilder().local(false).settings(settings).build().start()
  val client = node.client()
  val searchHitsSize = 1000

  def shutDown(): Unit = {
    node.stop.close()
  }

  def indexEntity[T <: BaseEntity](entity: T, index: String, indexType: String)(implicit formateObj: RootJsonFormat[T]): Unit = {
    entity.id.foreach { id =>
      val v = formateObj.write(entity).toString()
      val respo = client.prepareIndex(index.toLowerCase, indexType, id.toString).setSource(v).execute().actionGet()
    }
  }

  def boolQuery(esSearchQuery: EsSearchQuery): List[EsSearchResponse] = {
    val index = esSearchQuery.index.toLowerCase
    val searchRequest = client.prepareSearch(index)
    searchRequest.addField(esSearchQuery.outputField)
    esSearchQuery.types.foreach(searchType => searchRequest.setTypes(searchType))
    val query = new BoolQueryBuilder
    esSearchQuery.queryFields.foreach { queryField =>
      val qsQuery = QueryBuilders.queryString(addWildCard(queryField.value))
      qsQuery.field(addWildCard(queryField.key))
      qsQuery.analyzeWildcard(true)
      esSearchQuery.queryType match {
        case EsQueryType.OR =>
          query.should(qsQuery)
        case EsQueryType.AND =>
          query.must(qsQuery)
      }
    }
    searchRequest.setQuery(query)
    searchRequest.setSize(searchHitsSize)
    val response = searchRequest.execute.actionGet
    response.getHits.toList.map { hit =>
      EsSearchResponse(hit.getType, esSearchQuery.outputField, hit.getId)
    }
  }

  def addWildCard(queryString: String): String = {
    if ("_all" == queryString) queryString
    val wildCardStr = new StringBuilder
    if (!queryString.startsWith("*")) wildCardStr.append("*")
    if (!queryString.endsWith("*")) wildCardStr.append(queryString).append("*")
    wildCardStr.toString
  }

  def fieldMappings(indexName: String, mappingType: String): Set[String] = {
    val index = indexName.toLowerCase
    val mayBeClusterState = Option(client.admin())
      .flatMap(admin => Option(admin.cluster()))
      .flatMap(cluster => Option(cluster.prepareState()))
      .flatMap(state => Option(state.setFilterIndices(index)))
      .flatMap(filter => Option(filter.execute()))
      .flatMap(execution => Option(execution.actionGet()))
      .flatMap(action => Option(action.getState))
    //client.admin().cluster().prepareState().setFilterIndices(index).execute().actionGet().getState
    val mayBeMapping = mayBeClusterState
      .flatMap(clusterState => Option(clusterState.getMetaData))
      .flatMap(metaData => Option(metaData.index(index)))
      .flatMap(index => Option(index.mapping(mappingType)))
    //getMetaData.index(index).mapping(mappingType)\
    mayBeMapping match {
      case Some(mapping) => flattenKeys(mapping.getSourceAsMap, mappingType)
      case None => Set.empty[String]
    }

  }

  def flattenKeys(map: java.util.Map[String, AnyRef], parent: String): Set[String] = {
    if (map.contains("properties")) {
      val properties = map("properties").asInstanceOf[java.util.Map[String, AnyRef]]
      properties.flatMap {
        case (key, value) =>
          val interMap = value.asInstanceOf[java.util.Map[String, AnyRef]]
          if (interMap.contains("dynamic")) {
            flattenKeys(interMap, parent + "." + key)
          } else {
            Set(parent)
          }
      }.toSet
    } else {
      Set(parent)
    }
  }

}