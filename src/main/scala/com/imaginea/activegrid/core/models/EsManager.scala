package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.node.NodeBuilder
import org.slf4j.LoggerFactory
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
  val mayBeNode = Option(NodeBuilder.nodeBuilder().local(false).settings(settings).build())
    .flatMap(node => Option(node.start()))
  val mayBeClient = mayBeNode.flatMap(node => Option(node.client()))
  val searchHitsSize = 1000
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def shutDown(): Unit = {
    mayBeNode match {
      case Some(node) => node.stop().close()
      case None =>
        logger.warn("Unable to load the node")
        throw new Exception("Unable to load the node")
    }

  }

  def indexEntity[T <: BaseEntity](entity: T, index: String, indexType: String)(implicit formateObj: RootJsonFormat[T]): Unit = {
    entity.id.foreach { id =>
      Option(formateObj.write(entity)).foreach { jsValue =>
        mayBeClient.foreach { client =>
          Option(client.prepareIndex(index.toLowerCase, indexType, id.toString))
            .flatMap(indexRequest => Option(indexRequest.setSource(jsValue.toString())))
            .flatMap(indexRequest => Option(indexRequest.execute()))
            .foreach(actionFuture => actionFuture.actionGet())
        }
      }
    }
  }

  def boolQuery(esSearchQuery: EsSearchQuery): List[EsSearchResponse] = {
    val index = esSearchQuery.index.toLowerCase
    mayBeClient match {
      case Some(client) => val searchRequest = client.prepareSearch(index)
        searchRequest.addField(esSearchQuery.outputField)
        esSearchQuery.types.foreach(searchType => searchRequest.setTypes(searchType))
        val query = new BoolQueryBuilder
        esSearchQuery.queryFields.foreach {
          queryField =>
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
        response.getHits.toList.map {
          hit =>
            EsSearchResponse(hit.getType, esSearchQuery.outputField, hit.getId)
        }
      case None =>
        logger.warn("Unable to load the client")
        throw new Exception("Unable to load the client")
    }
  }

  def addWildCard(queryString: String): String = {
    if ("_all".equals(queryString)) {
      queryString
    } else {
      val wildCardStr = new StringBuilder
      if (!queryString.startsWith("*")) wildCardStr.append("*").append(queryString)
      if (!queryString.endsWith("*")) wildCardStr.append(queryString).append("*")
      wildCardStr.toString
    }
  }

  def fieldMappings(indexName: String, mappingType: String): Set[String] = {
    val index = indexName.toLowerCase
    mayBeClient match {
      case Some(client) =>
        val mayBeClusterState = Option(client.admin())
          .flatMap(admin => Option(admin.cluster()))
          .flatMap(cluster => Option(cluster.prepareState()))
          .flatMap(state => Option(state.setFilterIndices(index)))
          .flatMap(filter => Option(filter.execute()))
          .flatMap(execution => Option(execution.actionGet()))
          .flatMap(action => Option(action.getState))
        val mayBeMapping = mayBeClusterState
          .flatMap(clusterState => Option(clusterState.getMetaData))
          .flatMap(metaData => Option(metaData.index(index)))
          .flatMap(index => Option(index.mapping(mappingType)))
        mayBeMapping match {
          case Some(mapping) => flattenKeys(mapping.getSourceAsMap, mappingType)
          case None =>
            logger.warn(s"No mappings found with $mappingType")
            Set.empty[String]
        }
      case None =>
        logger.warn("Unable to load the client")
        throw new Exception("Unable to load the client")
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