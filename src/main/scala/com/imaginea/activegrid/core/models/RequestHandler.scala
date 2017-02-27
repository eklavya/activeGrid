package com.imaginea.activegrid.core.models

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp, UnreachableMember}
import org.slf4j.LoggerFactory
import RequestHandler._ //scalastyle:ignore underscore.import
/**
  * Created by shareefn on 27/2/17.
  */
class RequestHandler extends Actor {

  val logger = LoggerFactory.getLogger(getClass)
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }

  override def receive: PartialFunction[Any, Unit] = {
    case MemberUp(member) =>
      logger.info("Member is Up: {}", member.address)

    case ListOfImages =>
      val imageLabel: String = "ImageInfo"
      val nodesList = Neo4jRepository.getNodesByLabel(imageLabel)
      val imageInfoList = nodesList.flatMap(node => ImageInfo.fromNeo4jGraph(node.getId))

      sender() ! Page[ImageInfo](imageInfoList)
  }
}

object RequestHandler {
  case object ListOfImages
}