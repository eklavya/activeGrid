package com.imaginea.activegrid.core.models

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator}
import akka.cluster.ddata.Replicator._ //scalastyle:ignore underscore.import
import akka.pattern.ask
import com.typesafe.scalalogging.Logger
import scala.concurrent.duration._ //scalastyle:ignore underscore.import
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by shareefn on 7/3/17.
  */
object SharedSiteCache {

  implicit val system = ActorSystem("ClusterSystem")
  implicit val timeout = akka.util.Timeout(10.seconds)
  val replicator = DistributedData(system).replicator
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  implicit val cluster = Cluster(system)

  def dataKey(entryKey: String): LWWMapKey[Site1] =
    LWWMapKey(entryKey)

  def putSite(key: String, value: Site1): Unit = {
    logger.info(s"inserting site in shared map with key: $key")
    replicator ! Replicator.Update(dataKey(key), LWWMap(), writeLocal)(_ + (key -> value))
  }

  def removeSite(key: String): Unit = {
    logger.info(s"removing site in shared map with key: $key")
    replicator ! Replicator.Update(dataKey(key), LWWMap(), writeLocal)(_ - key)
  }

  def getSite(key: String): Future[Option[Site1]] = {
    logger.info(s"getting site in shared map with key: $key")
    val futureResult = replicator ? Get(dataKey(key), readLocal, Some(key))
    futureResult.map {
      case g @ GetSuccess(LWWMapKey(_), Some(k: String)) =>
        g.dataValue match {
          case data: LWWMap[Site1] => data.get(k)
        }
      case NotFound(_, Some(k)) =>
        logger.info(s"key: $k not found")
        None
    }
  }
}

