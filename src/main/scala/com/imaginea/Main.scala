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

  implicit val ImageFormat = jsonFormat(ImageInfo.apply, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
  implicit val PageImageInfoFormat = jsonFormat(Page[ImageInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val KeyPairInfoFormat = jsonFormat(KeyPairInfo.apply, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val PageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val UserFormat = jsonFormat(User.apply, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val PageUsersFomat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")

  implicit val ResourceACLFormat = jsonFormat1(ResourceACL.apply)
  implicit val UserGroupFormat = jsonFormat(UserGroup.apply, "id", "name", "users", "accesses")

  implicit val SSHKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")

  val userService: UserService = new UserService

  def userRoute: Route = pathPrefix("users" / LongNumber) { userId =>
    pathPrefix("keys") {
      pathPrefix(LongNumber) { keyId =>
        get {
          val key = userService.getKey(userId, keyId)
          onComplete(key) {
            case util.Success(response) => {
              response match {
                case Some(keyPairInfo) => complete(StatusCodes.OK, keyPairInfo)
                case None => complete(StatusCodes.BadRequest, "Unable to get the key")
              }
            }
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to get the key, Reason: ${ex.getMessage}")
          }

        } ~ delete {
          onComplete(userService.deleteKey(userId, keyId)) {
            case util.Success(result) => complete(StatusCodes.OK, "Deleted Successfully")
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed delete, Message: ${ex.getMessage}")
          }
        }
      } ~ get {
        onComplete(userService.getKeys(userId)) {
          case util.Success(page) => complete(StatusCodes.OK, page)
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed get Users, Message: ${ex.getMessage}")
        }
      } ~ post {
        entity(as[SSHKeyContentInfo]) { sshKeyInfo =>
          onComplete(userService.addKeyPair(userId, sshKeyInfo)) {
            case util.Success(page) => complete(StatusCodes.OK, page)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to add keys, Message: ${ex.getMessage}")
          }
        }
      }
    } ~ get {
      onComplete(userService.getUser(userId)) {
        case util.Success(user) => complete(StatusCodes.OK, user)
        case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get user, Message: ${ex.getMessage}")
      }
    } ~ delete {
      onComplete(userService.deleteUser(userId)) {
        case util.Success(status) => complete(StatusCodes.OK, "Deleted succesfully")
        case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to delete user, Message: ${ex.getMessage}")
      }
    }
  } ~ pathPrefix("users") {
    get {
      pathPrefix(Segment / "keys") { userName =>
        onComplete(userService.getKeys(userName)) {
          case util.Success(page) => complete(StatusCodes.OK, page)
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get keys, Message: ${ex.getMessage}")
        }
      }
    } ~ get {
      onComplete(userService.getUsers) {
        case util.Success(page) => complete(StatusCodes.OK, page)
        case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get users, Message: ${ex.getMessage}")
      }
    } ~ post {
      entity(as[User]) { user =>
        onComplete(userService.saveUser(user)) {
          case util.Success(status) => complete(StatusCodes.OK, "Successfully saved user")
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed save user, Message: ${ex.getMessage}")
        }
      }
    } ~ pathPrefix("groups") {
      post {
        entity(as[UserGroup]) { userGroup =>
          onSuccess(userService.saveUserGroup(userGroup)) {
            case Some(response) => complete(StatusCodes.OK, response)
            case None => complete(StatusCodes.BadRequest, "Unable save user group ")
          }
        }
      }
    }
  }


  val route: Route = userRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
