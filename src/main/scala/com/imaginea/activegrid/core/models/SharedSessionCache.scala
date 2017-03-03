package com.imaginea.activegrid.core.models

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey, Replicator}
import akka.cluster.ddata.Replicator._ //scalastyle:ignore underscore.import
import scala.concurrent.duration._  //scalastyle:ignore underscore.import
import SharedSessionCache._ //scalastyle:ignore underscore.import

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
  implicit val cluster = Cluster(context.system)

  def dataKey(entryKey: String): LWWMapKey[TerminalSession] =
    LWWMapKey("cache-" + math.abs(entryKey.hashCode) % 100)

  def receive: PartialFunction[Any, Unit] = {
    case PutSession(key, value) =>
      replicator ! Replicator.Update(dataKey(key), LWWMap(), writeMajority)(_ + (key -> value))
    case RemoveSession(key) =>
      replicator ! Replicator.Update(dataKey(key), LWWMap(), writeMajority)(_ - key)
    case GetSession(key) =>
      replicator ! Get(dataKey(key), readMajority, Some(Request(key, sender())))
    case g @ GetSuccess(LWWMapKey(_), Some(Request(key, replyTo))) =>
      g.dataValue match {
        case data: LWWMap[TerminalSession] => data.get(key) match {
          case Some(value) => replyTo ! Some(value)
          case None        => replyTo ! None
        }
      }
    case NotFound(_, Some(Request(key, replyTo))) =>
      replyTo ! None
    case _: UpdateResponse[_] => // ok
  }

}

