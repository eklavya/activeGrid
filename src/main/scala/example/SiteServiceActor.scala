package example

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by sivag on 27/2/17.
  */
class SiteServiceActor  extends Actor {
  override def receive: Receive = {
    case "Message" => println("Site Service Received message "+self.path)
  }
}
