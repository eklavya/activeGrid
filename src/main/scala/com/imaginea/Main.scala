package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.models.KeyPairStatus.{KeyPairStatus, _}
import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.services.{CatalogService, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}
/**
  * Created by babjik on 22/9/16.
  */
object Main extends App {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val config = ConfigFactory.load
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val neo4jRepository = Neo4jRepository

  implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {
    override def write (obj: KeyPairStatus): JsValue = JsString(obj.toString)

    override def read(json: JsValue): KeyPairStatus = json match {
      case JsString(str) => KeyPairStatus.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit val ImageFormat = jsonFormat(ImageInfo, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
  implicit val pageImageInfoFomat = jsonFormat(Page[ImageInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val keyPairInfoFormat = jsonFormat(KeyPairInfo, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val pageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val userFormat = jsonFormat(User, "id", "userName", "password", "email", "uniqueId",  "publicKeys",  "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val pageUsersFomat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")

  implicit val ResourceACLFormat = jsonFormat(ResourceACL, "id")
  implicit val UserGroupFormat = jsonFormat(UserGroup, "id", "name", "users", "accesses")

  implicit val SSHKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")

  val userService: UserService = new UserService


  def userRoute: Route = pathPrefix("users" / LongNumber){ userId =>
      get{
        pathPrefix("groups") {
          complete("found groups")
        }
      } ~ pathPrefix("keys") {
        get {
          try {
            complete(userService.getKeys(userId))
          } catch {
            case _: Throwable => {
              complete(s"failed to get keys of User ${userId}")
            }
          }
        } ~ post {
          entity(as[SSHKeyContentInfo]) { sshKeyInfo =>
            complete(sshKeyInfo)
          }
        }
      }~ get{
        try {
          complete(userService.getUser(userId))
        } catch {
          case e: NotFoundException => {
            logger.warn(e.getMessage, e)
            complete(e.getMessage)
          }
          case _ : Throwable => {
            complete("Unhandled exception")
          }
        }

      } ~ delete {
        complete(userService.deleteUser(userId))
      }
    } ~pathPrefix("users") {
    get {
      complete(userService.getUsers)
    } ~ post {
      entity(as[User]) {user =>
          complete(userService.saveUser(user))
      }
    } ~ pathPrefix("groups") {
      post {
        entity(as[UserGroup]) { userGroup =>
          complete(userService.saveUserGroup(userGroup))
        }
      }
    }
  }



  val route: Route = userRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
