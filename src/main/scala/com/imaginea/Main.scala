package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.models.KeyPairStatus.KeyPairStatus
import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.services.UserService
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.util.{Success, Failure}

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


  implicit val ImageFormat = jsonFormat(ImageInfo.apply, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
  implicit val PageImageInfoFormat = jsonFormat(Page[ImageInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val KeyPairInfoFormat = jsonFormat(KeyPairInfo.apply, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val PageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")


  implicit val UserFormat = jsonFormat(User.apply, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val PageUsersFormat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")

  implicit val ResourceACLFormat = jsonFormat3(ResourceACL.apply)
  implicit val UserGroupFormat = jsonFormat(UserGroup.apply, "id", "name", "users", "accesses")
  implicit val PageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")

  implicit val ResponseUserGroupFormat = jsonFormat(SuccessResponse, "id")

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
      get { //Handling Future of Option[UserGroup]
        onSuccess(userService.getUserGroup(id)) {
          case None => complete(StatusCodes.NoContent, None)
          case Some(group) => complete(StatusCodes.OK, group)
        }
      } ~
      delete { //Handling Future of Unit
        onComplete(userService.deleteUserGroup(id)) {
          case Success(result) => complete(StatusCodes.OK, "Successfully deleted")
          case Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to delete, Exception: ${ex.getMessage}")
        }
      }
    } ~
      path("groups") {
        get { //Handling Future of Page[UserGroup]
          complete(userService.getUserGroups)
        } ~
          post { //Handling Future of sealed ResponseMessage type
            entity(as[UserGroup]) { userGroup =>
              onSuccess(userService.saveUserGroup(userGroup)) {
                case FailureResponse => complete(StatusCodes.BadRequest, None)
                case a: SuccessResponse => complete(StatusCodes.OK, a)
              }
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
    } /*~
    path("access") {
      post {
        entity(as[SiteACL]) { siteACL =>
          onSuccess(userService.createACL(siteACL)) {
            case None => complete(StatusCodes.NoContent, None)
            case Some(group) => complete(StatusCodes.OK, group)
          }
        }
      }
    }*/
  }

  val route: Route = userRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
