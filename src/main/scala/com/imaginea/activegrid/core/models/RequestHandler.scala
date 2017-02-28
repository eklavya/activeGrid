package com.imaginea.activegrid.core.models

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

/**
  * Created by nagulmeeras on 28/02/17.
  */
class RequestHandler extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def receive: Receive = {
    case appsettings: ApplicationSettings =>
      log.info("Gettings Application settings" )
      appsettings.toNeo4jGraph(appsettings)
      log.info("Saved Application settings !")
      sender() !  Message("Settings saved !")
    case MemberUp =>
      log.info("Member is UP ")
    case MemberJoined(member) =>
      log.info(s"new member is joined into cluster ${member.address}")
    case MemberLeft(member) =>
      log.info(s"Member is left from cluster ${member.address}")
    case MemberRemoved(member , status) =>
      log.info(s"Member $member is removed from cluster from status $status")
    case UnreachableMember(member) =>
      log.info("Member is unreachable to cluster")
    case _ =>
      log.info("Unknown message is recieved")
  }
}
