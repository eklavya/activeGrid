package scala.example

import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.routing._
import com.imaginea.Main
import example.{SiteServiceActor, Work}



import scala.example.MyfirstActor

class Master extends Actor {
  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[MyfirstActor])
      context watch r
      ActorRefRoutee(r)
    }
    val routes = Main.workflowRoutes
    Router(RoundRobinRoutingLogic(), routees)
  }
  val r2: ActorRef = context.actorOf(RoundRobinPool(5).props(Props[SiteServiceActor]), "router1")

  def receive = {
    case "SayHi" =>
      router.route("WelcomeScala", sender())
    case "Site" =>
      r2 ! "Message"
    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[MyfirstActor])
      context watch r
      router = router.addRoutee(r)
  }
}