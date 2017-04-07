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
  * Created by shareefn on 3/3/17.
  */
object SharedSessionCache {

  implicit val system = ActorSystem("ClusterSystem")
  implicit val timeout = akka.util.Timeout(10.seconds)
  val replicator = DistributedData(system).replicator
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  implicit val cluster = Cluster(system)

  def dataKey(entryKey: String): LWWMapKey[TerminalSession] =
    LWWMapKey(entryKey)

  def putSession(key: String, value: TerminalSession): Unit = {
    logger.info(s"inserting terminal session in shared map with key: $key")
    replicator ! Replicator.Update(dataKey(key), LWWMap(), writeLocal)(_ + (key -> value))
  }

  def removeSession(key: String): Unit = {
    logger.info(s"removing terminal session in shared map with key: $key")
    replicator ! Replicator.Update(dataKey(key), LWWMap(), writeLocal)(_ - key)
  }

  def getSession(key: String): Future[Option[TerminalSession]] = {
    logger.info(s"getting terminal session in shared map with key: $key")
    val futureResult =  replicator ? Get(dataKey(key), readLocal, Some(key))
    futureResult.map {
      case g @ GetSuccess(LWWMapKey(_), Some(k: String)) =>
        g.dataValue match {
          case data: LWWMap[TerminalSession] => data.get(k)
        }
      case NotFound(_, Some(k)) =>
        logger.info(s"key: $k not found")
        None
    }
  }
}

