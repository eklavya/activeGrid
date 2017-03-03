package com.imaginea.activegrid.core.models

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator}
import akka.cluster.ddata.Replicator._ //scalastyle:ignore underscore.import
import com.typesafe.scalalogging.Logger
import scala.concurrent.duration._  //scalastyle:ignore underscore.import
import SharedSessionCache._ //scalastyle:ignore underscore.import
import org.slf4j.LoggerFactory
/**
  * Created by shareefn on 3/3/17.
  */
object SharedSessionCache {

  val props: Props = Props[SharedSessionCache]

  case class Request(key: String, replyTo: ActorRef)
  case class PutSession(key: String, value: TerminalSession)
  case class GetSession(key: String)
  case class RemoveSession(key: String)

  val timeout = 10.seconds
  val readMajority = ReadMajority(timeout)
  val writeMajority = WriteMajority(timeout)
}

class SharedSessionCache extends Actor {

  val replicator = DistributedData(context.system).replicator
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  implicit val cluster = Cluster(context.system)

  def dataKey(entryKey: String): LWWMapKey[TerminalSession] =
    LWWMapKey(entryKey)

  def receive: PartialFunction[Any, Unit] = {
    case PutSession(key, value) =>
      logger.info(s"inserting terminal session in shared map with key: $key")
      replicator ! Replicator.Update(dataKey(key), LWWMap(), writeMajority)(_ + (key -> value))
    case RemoveSession(key) =>
      logger.info(s"removing terminal session in shared map with key: $key")
      replicator ! Replicator.Update(dataKey(key), LWWMap(), writeMajority)(_ - key)
    case GetSession(key) =>
      logger.info(s"getting terminal session in shared map with key: $key")
      replicator ! Get(dataKey(key), readMajority, Some(Request(key, sender())))
    case g @ GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[TerminalSession] => data.get(key) match {
          case Some(value) => replyTo ! Some(value)
          case None        => replyTo ! None
        }
      }
    case NotFound(_, Some(Request(key, replyTo))) =>
      logger.info(s"key: $key not found")
      replyTo ! None
    case _: UpdateResponse[_] => // ok
  }

}

