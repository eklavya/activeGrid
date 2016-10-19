package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.{Multipart, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.utils.{Constants, FileUtils}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.concurrent.Future

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


  def userRoute: Route = pathPrefix("users" / LongNumber) { userId =>
    pathPrefix("keys") {
      pathPrefix(LongNumber) { keyId =>
        get {
          val key = Future {
            getKeyById(userId, keyId)
          }
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
          val resposne = Future {
            logger.debug(s"Deleting Key[$keyId] of User[$userId] ")
            val key = getKeyById(userId, keyId)

            key match {
              case Some(keyPairInfo) =>
                val status = Neo4jRepository.deleteChildNode(keyId)
                status match {
                  case Some(false) | None => throw new Exception(s"No key pair found with id $keyId")
                  case Some(true) => // do nothing
                }
              case None => throw new Exception(s"No key pair found with id $keyId")
            }
          }
          onComplete(resposne) {
            case util.Success(result) => complete(StatusCodes.OK, "Deleted Successfully")
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed delete, Message: ${ex.getMessage}")
          }
        }
      } ~ get {
        val result = Future {
          User.fromNeo4jGraph(Some(userId)) match {
            case Some(user) => Page[KeyPairInfo](user.publicKeys)
            case None => Page[KeyPairInfo](List.empty[KeyPairInfo])
          }
        }
        onComplete(result) {
          case util.Success(page) => complete(StatusCodes.OK, page)
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed get Users, Message: ${ex.getMessage}")
        }
      } ~ post {
        entity(as[SSHKeyContentInfo]) { sshKeyInfo =>
          val result = Future {

            FileUtils.createDirectories(UserUtils.getKeyDirPath(userId))

            val resultKeys = sshKeyInfo.keyMaterials.map { case (keyName: String, keyMaterial: String) =>
              logger.debug(s" ($keyName  --> ($keyMaterial))")
              val filePath: String = UserUtils.getKeyFilePath(userId, keyName)
              FileUtils.saveContentToFile(filePath, keyMaterial)

              val keyPairInfo = KeyPairInfo(keyName, keyMaterial, Some(filePath), UploadedKeyPair)
              logger.debug(s" new Key Pair Info $keyPairInfo")
              UserUtils.addKeyPair(userId, keyPairInfo)
              keyPairInfo
            }

            Page(resultKeys.toList)
          }
          onComplete(result) {
            case util.Success(page) => complete(StatusCodes.OK, page)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to add keys, Message: ${ex.getMessage}")
          }
        }
      }
    } ~ get {
      val result = Future {
        User.fromNeo4jGraph(Some(userId))
      }
      onComplete(result) {
        case util.Success(mayBeUser) =>
          mayBeUser match  {
            case Some(user) => complete(StatusCodes.OK, user)
            case None => complete(StatusCodes.BadRequest, s"Failed to get user with id $userId")
          }
        case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get user, Message: ${ex.getMessage}")
      }
    } ~ delete {
      val result = Future {
        Neo4jRepository.deleteEntity(userId)
      }
      onComplete(result) {
        case util.Success(status) => complete(StatusCodes.OK, "Deleted succesfully")
        case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to delete user, Message: ${ex.getMessage}")
      }
    }
  } ~ pathPrefix("users") {
    get {
      pathPrefix(Segment / "keys") { userName =>
        val result = Future {
          logger.debug(s"Searching Users with name $userName")
          val maybeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("User", "username", userName)

          logger.debug(s" May be node $maybeNode")

          maybeNode match {
            case None => Page(List.empty[KeyPairInfo])
            case Some(node) =>
              User.fromNeo4jGraph(Some(node.getId)) match {
                case Some(user) => Page[KeyPairInfo](user.publicKeys)
                case None => Page[KeyPairInfo](List.empty[KeyPairInfo])
              }
          }
        }
        onComplete(result) {
          case util.Success(page) => complete(StatusCodes.OK, page)
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get keys, Message: ${ex.getMessage}")
        }
      }
    } ~ get {
      val result = Future {
        val nodeList = Neo4jRepository.getNodesByLabel("User")
        val listOfUsers = nodeList.flatMap(node => User.fromNeo4jGraph(Some(node.getId)))

        Page[User](listOfUsers)
      }
      onComplete(result) {
        case util.Success(page) => complete(StatusCodes.OK, page)
        case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get users, Message: ${ex.getMessage}")
      }
    } ~ post {
      entity(as[User]) { user =>
        val result = Future {
          user.toNeo4jGraph(user)
        }
        onComplete(result) {
          case util.Success(status) => complete(StatusCodes.OK, "Successfully saved user")
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed save user, Message: ${ex.getMessage}")
        }
      }
    }
  }

  //KeyPair Serivce
  def keyPairRoute: Route = pathPrefix("keypairs") {
      pathPrefix(LongNumber) { keyId =>
        get {
          val result = Future {
            val mayBeBode = Neo4jRepository.findNodeByLabelAndId("KeyPairInfo", keyId)
            mayBeBode match {
              case Some(node) => KeyPairInfo.fromNeo4jGraph(Some(node.getId))
              case None => None
            }
          }
          onComplete(result) {
            case util.Success(mayBekey) =>
              mayBekey match {
                case Some(key) => complete(StatusCodes.OK, key)
                case None => complete(StatusCodes.BadRequest, s"failed to get key pair for id $keyId")
              }
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get Key Pair, Message: ${ex.getMessage}")
          }
        } ~ delete {
          val result = Future {
            Neo4jRepository.deleteChildNode(keyId)
          }
          onComplete(result) {
            case util.Success(key) => complete(StatusCodes.OK, "Deleted Successfully")
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to delete Key Pair, Message: ${ex.getMessage}")
          }
        }
      } ~ get {
        val result = Future {
          val nodeList = Neo4jRepository.getNodesByLabel("KeyPairInfo")
          val listOfKeys = nodeList.flatMap(node => KeyPairInfo.fromNeo4jGraph(Some(node.getId)))
          Page[KeyPairInfo](0, listOfKeys.size, listOfKeys.size, listOfKeys)
        }
        onComplete(result) {
          case util.Success(page) => complete(StatusCodes.OK, page)
          case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get Keys, Message: ${ex.getMessage}")
        }
      } ~ put {
          entity(as[Multipart.FormData]) { formData =>
            val result = Future {
              val dataMap = formData.asInstanceOf[FormData.Strict].strictParts.map(strict => {

                val name = strict.getName()
                val value = strict.entity.getData().decodeString("UTF-8")
                val optionalFileName = strict.getFilename()
                logger.debug(s"--- $name  -- $value -- $optionalFileName")

                if (optionalFileName.equals(java.util.Optional.empty)) {
                  logger.debug(s" simple field")
                  name match {
                    case "userName" | "passPhase" => (name, value)
                    case _ => (new String, new String)
                  }
                } else {
                  logger.debug(s"reading from the file $optionalFileName")
                  (name, value)
                }

              }).filter{case (k, v) => !k.isEmpty}.toMap[String, String]

              val sshKeyContentInfo: SSHKeyContentInfo = SSHKeyContentInfo(dataMap)
              logger.debug(s"ssh info   - $sshKeyContentInfo")
              logger.debug(s"Data Map --- $dataMap")
              val userNameLabel = "userName"
              val passPhaseLabel = "passPhase"
              val addedKeyPairs: List[KeyPairInfo] = dataMap.map { case (keyName, keyMaterial) =>
                if (!userNameLabel.equalsIgnoreCase(keyName) && !passPhaseLabel.equalsIgnoreCase(keyName)) {
                  val keyPairInfo = getOrCreateKeyPair(keyName, keyMaterial, None, UploadedKeyPair, dataMap.get(userNameLabel), dataMap.get(passPhaseLabel))
                  val mayBeNode = saveKeyPair(keyPairInfo)
                  mayBeNode match {
                    case Some(key) => key
                    case _ => // do nothing
                  }
                }
              }.toList.collect { case x: KeyPairInfo => x }

              Page[KeyPairInfo](addedKeyPairs)
            }
            onComplete(result) {
              case util.Success(page) => complete(StatusCodes.OK, page)
              case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to update Keys, Message: ${ex.getMessage}")
            }
          }
      }
  }

  val route: Route = userRoute ~ keyPairRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")


  def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    User.fromNeo4jGraph(Some(userId)) match {
      case Some(user) =>
        val keysList: List[KeyPairInfo] = user.publicKeys
        keysList match {
          case keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
          case _ :: keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
          case _ => None
        }
      case None => None
    }
  }

  def getOrCreateKeyPair(keyName: String, keyMaterial: String, keyFilePath: Option[String], status: KeyPairStatus, defaultUser: Option[String], passPhase: Option[String]): KeyPairInfo = {
    val mayBeKeyPair = getKeyPair(keyName)

    mayBeKeyPair match {
      case Some(keyPairInfo) =>
        KeyPairInfo(keyPairInfo.id, keyName, keyPairInfo.keyFingerprint, keyMaterial, if (keyFilePath.isEmpty) keyPairInfo.filePath else keyFilePath, status, if (defaultUser.isEmpty) keyPairInfo.defaultUser else defaultUser, if (passPhase.isEmpty) keyPairInfo.passPhrase else passPhase)
      case None => KeyPairInfo(keyName, keyMaterial, keyFilePath, status)
    }
  }

  def saveKeyPair(keyPairInfo: KeyPairInfo): Option[KeyPairInfo] = {
    val filePath = getKeyFilePath(keyPairInfo.keyName)
    try {
      FileUtils.createDirectories(getKeyFilesDir)
      FileUtils.saveContentToFile(filePath, keyPairInfo.keyMaterial)
      // TODO: change permissions to 600
    } catch {
      case e: Throwable => logger.error(e.getMessage, e)
    }
    val mayBeNode = keyPairInfo.toNeo4jGraph(KeyPairInfo(keyPairInfo.id, keyPairInfo.keyName, keyPairInfo.keyFingerprint, keyPairInfo.keyMaterial, Some(filePath), keyPairInfo.status, keyPairInfo.defaultUser, keyPairInfo.passPhrase))
    mayBeNode match {
      case Some(node) => KeyPairInfo.fromNeo4jGraph(Some(node.getId))
      case None => None
    }
  }

  def getKeyFilesDir: String = s"${Constants.getTempDirectoryLocation}${Constants.FILE_SEPARATOR}"

  def getKeyFilePath(keyName: String) = s"$getKeyFilesDir$keyName.pem"

  def getKeyPair(keyName: String): Option[KeyPairInfo] = {
    val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty("KeyPairInfo", "keyName", keyName)
    mayBeNode match {
      case Some(node) => KeyPairInfo.fromNeo4jGraph(Some(node.getId))
      case None => None
    }
  }
}
