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
import com.imaginea.activegrid.core.utils.Constants
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
    override def write(obj: KeyPairStatus): JsValue = JsString(obj.toString)

    override def read(json: JsValue): KeyPairStatus = json match {
      case JsString(str) => KeyPairStatus.withName(str)
      case _ => throw new DeserializationException("Enum string expected")
    }
  }

  implicit val ImageFormat = jsonFormat(ImageInfo, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
  implicit val pageImageInfoFormat = jsonFormat(Page[ImageInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val keyPairInfoFormat = jsonFormat(KeyPairInfo, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val pageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val userFormat = jsonFormat(User, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val pageUsersFormat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")

  // Json format implicit object import for UserGroup
  //implicit val ResourceACLFormat = jsonFormat(ResourceACL, "id")
  implicit val userGroupResourceFormat = jsonFormat3(ResourceACL)
  implicit val userGroupFormat = jsonFormat(UserGroup, "id", "name", "users", "accesses")
  implicit val pageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")

  implicit val SSHKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")


  val userService: UserService = new UserService

  def userRoute: Route = pathPrefix("users" / LongNumber) { userId =>
    get {
      pathPrefix("groups") {
        // TODO: need add code related to groups
        complete("found groups")
      }
    } ~ pathPrefix("keys") {
      pathPrefix(LongNumber) { keyId =>
        get {
          try {
            complete(userService.getKey(userId, keyId))
          } catch {
            case _: Throwable => {
              complete(s"failed to get key")
            }
          }
        } ~ delete {
          complete(userService.deleteKey(userId, keyId))
        }
      } ~ get {
        try {
          complete(userService.getKeys(userId))
        } catch {
          case _: Throwable => {
            complete(s"failed to get keys of User ${userId}")
          }
        }
      } ~ post {
        entity(as[SSHKeyContentInfo]) { sshKeyInfo =>
          complete(userService.addKeyPair(userId, sshKeyInfo))
        }
      }
    } ~ get {
      try {
        complete(userService.getUser(userId))
      } catch {
        case e: NotFoundException => {
          logger.warn(e.getMessage, e)
          complete(e.getMessage)
        }
        case _: Throwable => {
          complete("Unhandled exception")
        }
      }

    } ~ delete {
      complete(userService.deleteUser(userId))
    }
  } ~ pathPrefix("users") {
    path("groups" / LongNumber) { id =>
      get {
        onSuccess(userService.getUserGroup(id)) {
          case None => complete(StatusCodes.NoContent, None)
          case Some(group) => complete(StatusCodes.OK, group)
        }
      }
    } ~
      path("groups") {
        get {
          complete(userService.getUserGroups)
          //complete("Done")
        } ~
          post {
            entity(as[UserGroup]) { userGroup =>
              complete(userService.saveUserGroup(userGroup))
            }
          }
      } ~ get {
        pathPrefix(Segment / "keys") { userName =>
          complete(userService.getKeys(userName))
        }
      } ~ get {
        complete(userService.getUsers)
      } ~ post {
        entity(as[User]) { user =>
          complete(userService.saveUser(user))
        }
      }
  }

  val route: Route = userRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
