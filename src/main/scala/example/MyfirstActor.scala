package scala.example

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by sivag on 17/2/17.
  */
class MyfirstActor  extends Actor {

  override def preStart(): Unit = {
    super.preStart();

  }
  override def receive: Receive = {
    case "WelcomeScala" => println("Welcome to scala world!!!! by"+self.path)
    case _ => println("Pong got something unexpected.")
  }
}
