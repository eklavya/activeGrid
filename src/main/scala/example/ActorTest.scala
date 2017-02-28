package scala.example


import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatchers, Route}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}
/**
  * Created by sivag on 17/2/17.
  */
object ActorTest {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.parseString("akka.netty.remote.tcp.port="+2551).withFallback(ConfigFactory.load("activegrid.conf"))
    implicit val system = ActorSystem("ActorSystem",config)

    val actor = system.actorOf(Props[MyfirstActor],"MyFirstActor");
    val actor2  = system.actorOf(Props[Master],"Router")
    system.actorOf(Props[ClusterListener], name = "clusterListener")
    val ac = system.actorSelection("akka://MyTestActor/user/MyFirstActor");
   /* var i = 10
    while(i>0) {
      actor2 ! "SayHi"
      i = i - 1;
    }
    i = 10
    while(i>0) {
      actor2 ! "Site"
      i = i - 1;
    }*/
    implicit val dispatcher = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val route : Route = pathPrefix("activegrid"){
      path("prefix") {
        get {
          val s: Future[String] = Future {
            "Hi"
          }
          onComplete(s) {
            case Success(x) => complete(StatusCodes.OK, "Done successfully")
            case Failure(f) => complete(StatusCodes.BadRequest, "Processing failed")
          }
        }
      }
    }

    val s = Http().bindAndHandle(route,"localhost",8080)




  }
}
