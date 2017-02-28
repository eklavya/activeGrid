package scala.example

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, MemberEvent, MemberUp, UnreachableMember}

/**
  * Created by shareefn on 9/2/17.
  */
class ClusterListener extends Actor {

  val cluster = Cluster(context.system)
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  def receive = {
    case MemberUp(member) => {
      println("Member is Up: {}", member.address)
    }
    case x: String => println(s"received message $x")
    case _: MemberEvent => // ignore

  }
}


