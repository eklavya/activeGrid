package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.services.{KeyPairService, UserService}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
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

  implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {
    override def write(obj: KeyPairStatus): JsValue = JsString(obj.name.toString)

    override def read(json: JsValue): KeyPairStatus = json match {
      case JsString(str) => KeyPairStatus.toKeyPairStatus(str)
      case _ => throw DeserializationException("Enum string expected")
    }
  }

  implicit val KeyPairInfoFormat = jsonFormat(KeyPairInfo.apply, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val PageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val UserFormat = jsonFormat(User.apply, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val PageUsersFomat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")

  implicit val SSHKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")

  val userService: UserService = new UserService

  def userRoute: Route = pathPrefix("users" / LongNumber) { userId =>
    pathPrefix("keys") {
      pathPrefix(LongNumber) { keyId =>
        get {
          val key = userService.getKey(userId, keyId)
          onComplete(key) {
            case util.Success(response) =>
              response match {
                case Some(keyPairInfo) => complete(StatusCodes.OK, keyPairInfo)
                case None => complete(StatusCodes.BadRequest, "Unable to get the key")
              }
            case util.Failure(ex) =>
              logger.error(s"Unable to get the key, Reason: ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Unable to get the key, Reason: ${ex.getMessage}")
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
        case util.Success(mayBeUser) =>
          mayBeUser match  {
            case Some(user) => complete(StatusCodes.OK, user)
            case None => complete(StatusCodes.BadRequest, s"Failed to get user with id $userId")
          }
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
    }
  }

  //KeyPair Serivce
  val keyPairService = new KeyPairService
  def keyPairRoute: Route = pathPrefix("keypairs") {
      pathPrefix(LongNumber) { keyId =>
        get {
          onComplete(keyPairService.getKey(keyId)) {
            case util.Success(mayBekey) =>
              mayBekey match {
                case Some(key) => complete(StatusCodes.OK, key)
                case None => complete(StatusCodes.BadRequest, s"failed to get key pair for id $keyId")
              }
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get Key Pair, Message: ${ex.getMessage}")
          }
        } ~ delete {
          onComplete(keyPairService.deleteKeyById(keyId)) {
            case util.Success(key) => complete(StatusCodes.OK, "Deleted Successfully")
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to delete Key Pair, Message: ${ex.getMessage}")
          }
        }
      } ~ get {
        onComplete(keyPairService.getKeyPairs) {
          case util.Success(page) => complete(StatusCodes.OK, page)
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get Keys, Message: ${ex.getMessage}")
        }
      } ~ put {
          entity(as[Multipart.FormData]) { formData =>
            onComplete(keyPairService.uploadKeyPairs(formData)) {
              case util.Success(page) => complete(StatusCodes.OK, page)
              case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to update Keys, Message: ${ex.getMessage}")
            }
          }
      }
  }

  val route: Route = userRoute ~ keyPairRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
