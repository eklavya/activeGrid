package com.imaginea.activegrid.core.controller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.{LongNumber, Segment}
import akka.http.scaladsl.server._
import com.imaginea.activegrid.core.discovery.models.{Instance, Site}
import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.utils.FileUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * Created by babjik on 27/9/16.
 */
class UserServiceController(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val user: User = null

  implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {

    import KeyPairStatus._

    override def write(obj: KeyPairStatus): JsValue =
      JsString(obj.name.toString)

    override def read(json: JsValue): KeyPairStatus = json match {
      case JsString(str) => str
      case _ => throw new DeserializationException("Exception in keyPairStatus deserialization")
    }
  }

  implicit val KeyPairInfoFormat = jsonFormat(KeyPairInfo.apply, "id", "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
  implicit val PageKeyPairInfo = jsonFormat(Page[KeyPairInfo], "startIndex", "count", "totalObjects", "objects")

  implicit val UserFormat = jsonFormat(User.apply, "id", "userName", "password", "email", "uniqueId", "publicKeys", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
  implicit val PageUsersFormat = jsonFormat(Page[User], "startIndex", "count", "totalObjects", "objects")

  implicit val ResourceACLFormat = jsonFormat(ResourceACL.apply, "id", "resources", "permission", "resourceIds")
  implicit val UserGroupFormat = jsonFormat(UserGroup.apply, "id", "name", "users", "accesses")
  implicit val PageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")
  implicit val ResponseUserGroupFormat = jsonFormat(SuccessResponse, "id")

  implicit val SSHKeyContentInfoFormat = jsonFormat(SSHKeyContentInfo, "keyMaterials")


  implicit val InstanceFormat = jsonFormat(Instance.apply, "id", "instanceId", "name", "state", "platform", "architecture", "publicDnsName")
  implicit val SiteFormat = jsonFormat(Site.apply, "id", "siteName", "siteName")
  implicit val SiteACLFormat = jsonFormat(SiteACL.apply, "id", "name", "site", "instances", "groups")
  implicit val PageSiteACLFormat = jsonFormat(Page[SiteACL], "startIndex", "count", "totalObjects", "objects")

  def userRoute: Route = pathPrefix("users") {
    path("groups" / LongNumber) { id =>
      get {
        logger.debug("Get UserGroup by id")
        val userGroup: UserGroup = null
        val getUserGroup = Future {
          userGroup.fromNeo4jGraph(id)
        }
        onComplete(getUserGroup) {
          case Success(result) => complete(StatusCodes.OK, result)
          case Failure(ex) => ex match {
            case x: ApplicationException => complete(StatusCodes.NoContent, None)
            case x: Exception => complete(StatusCodes.BadRequest, s"Unable to fetch User group, Exception: ${ex.getMessage}")
          }
        }
      } ~
        delete {
          //Handling Future of Unit
          logger.debug("Delete UserGroup by id")
          val deleteUserGroup = Future {
            Neo4jRepository.removeEntity[UserGroup](id)
          }
          onComplete(deleteUserGroup) {
            case Success(result) => complete(StatusCodes.OK, "Successfully deleted")
            case Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to delete User group, Exception: ${ex.getMessage}")
          }
        }
    } ~
      path("groups") {
        get {
          //Handling Future of Page[UserGroup]
          logger.debug("Fetch all groups")
          val userGroups = Future {
            val nodeList = Neo4jRepository.getNodesByLabel(UserGroup.label)
            val userGroup: UserGroup = null
            val listOfUserGroups = nodeList.map(node => userGroup.fromNeo4jGraph(node.getId))
            Page[UserGroup](0, listOfUserGroups.size, listOfUserGroups.size, listOfUserGroups)
          }
          complete(userGroups)
        } ~
          post {
            //Handling Future of sealed ResponseMessage type
            entity(as[UserGroup]) { userGroup => {
              logger.debug("Create User Group")
              val saveUserGroup = Future {
                userGroup.toNeo4jGraph(userGroup)
              }
              onComplete(saveUserGroup) {
                case Success(node) => complete(StatusCodes.OK, new SuccessResponse(node.getId.toString()))
                case Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to save User group, Exception: ${ex.getMessage}")
              }
            }
            }
          }
      }
  } ~
    pathPrefix("users") {
      path("access" / LongNumber) { id =>
        get {
          //Handling Future of Option[SiteACL]
          logger.debug("Get user access by id")
          val accessFuture = Future {
            import com.imaginea.activegrid.core.models.SiteACL.RichSiteACL

            val siteAcl: SiteACL = null;
            siteAcl.fromNeo4jGraph(id)
          }
          onComplete(accessFuture) {
            case Success(result) => complete(StatusCodes.OK, result)
            case Failure(ex) => ex match {
              case x: ApplicationException => complete(StatusCodes.NoContent, None)
              case x: Exception => complete(StatusCodes.BadRequest, s"Unable to fetch User access for id = ${id}, Exception: ${ex.getMessage}")
            }
          }
        }
      } ~
        path("access") {
          get {
            //Handling Future of Page[SiteACL]
            logger.debug("Fetch all siteACL")
            def accessFuture = Future {
              val nodeList = Neo4jRepository.getNodesByLabel(UserGroup.label)
              val siteACL: SiteACL = null
              val listOfUserGroups: List[SiteACL] =
                nodeList.map {
                  node =>
                    siteACL.fromNeo4jGraph(node.getId)
                }
              Page[SiteACL](0, listOfUserGroups.size, listOfUserGroups.size, listOfUserGroups)
            }
            onComplete(accessFuture) {
              case Success(result) => complete(StatusCodes.OK, result)
              case Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to fetch User access , Exception: ${ex.getMessage}")
            }
          } ~
            post {
              //Handling Future of sealed ResponseMessage type
              entity(as[SiteACL]) { siteACL => {
                logger.debug("Create SiteACL" + siteACL)
                val accessFuture = Future {
                  siteACL.toNeo4jGraph(siteACL)
                }
                onComplete(accessFuture) {
                  case Success(node) => complete(StatusCodes.OK, new SuccessResponse(node.getId.toString()))
                  case Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to save SiteACL, Exception: ${ex.getMessage}")
                }
              }
              }
            }
        }
    }/* ~
    pathPrefix("users" / LongNumber) { userId =>
      pathPrefix("keys") {
        pathPrefix(LongNumber) { keyId =>
          get {
            val keyFuture = Future {
              getKeyById(userId, keyId)
            }
            onComplete(keyFuture) {
              case Success(response) => {
                response match {
                  case Some(keyPairInfo) => complete(StatusCodes.OK, keyPairInfo)
                  case None => complete(StatusCodes.BadRequest, "Unable to get the key")
                }
              }
              case Failure(ex) => {
                logger.error(s"Unable to get the key, Reason: ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, s"Unable to get the key, Reason: ${ex.getMessage}")
              }
            }

          } ~ delete {
            val deleteKeyFuture = Future {
              logger.debug(s"Deleting Key[${keyId}] of User[${userId}] ")
              val key = getKeyById(userId, keyId)

              key match {
                case Some(keyPairInfo) => {
                  val status = Neo4jRepository.deleteChildNode(keyId)
                  status match {
                    case Some(false) | None => throw new Exception(s"No key pair found with id ${keyId}")
                    case Some(true) => // do nothing
                  }
                }
                case None => throw new Exception(s"No key pair found with id ${keyId}")
              }
            }
            onComplete(deleteKeyFuture) {
              case Success(result) => complete(StatusCodes.OK, "Deleted Successfully")
              case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed delete, Message: ${ex.getMessage}")
            }
          }
        } ~ get {
          val keyFuture = Future {
            getKeyPairInfo(userId)
          }
          onComplete(keyFuture) {
            case Success(page) => complete(StatusCodes.OK, page)
            case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed get Users, Message: ${ex.getMessage}")
          }
        } ~ post {
          entity(as[SSHKeyContentInfo]) { sshKeyInfo =>
            onComplete(addKeyPair(userId, sshKeyInfo)) {
              case Success(page) => complete(StatusCodes.OK, page)
              case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to add keys, Message: ${ex.getMessage}")
            }
          }
        }
      } ~ get {
        val user: User = null
        val getUserFuture = Future {
          user.fromNeo4jGraph(userId)
        }
        onComplete(getUserFuture) {
          case Success(user) => complete(StatusCodes.OK, user)
          case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get user, Message: ${ex.getMessage}")
        }
      } ~ delete {
        val deleteUserFuture = Future {
          Neo4jRepository.deleteEntity(userId)
        }
        onComplete(deleteUserFuture) {
          case Success(status) => complete(StatusCodes.OK, "Deleted succesfuly")
          case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to delete user, Message: ${ex.getMessage}")
        }
      }
    } ~ pathPrefix("users") {
    get {
      pathPrefix(Segment / "keys") { userName =>
        val getKeysFuture = Future {
          logger.debug(s"Searching Users with name ${userName}")
          val node = Neo4jRepository.getSingleNodeByLabelAndProperty(User.label, "username", userName)
          getKeyPairInfo(node.getId)
        }
        onComplete(getKeysFuture) {
          case Success(page) => complete(StatusCodes.OK, page.toString) //Remove toString
          case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get keys, Message: ${ex.getMessage}")
        }
      }
    } ~ get {
      val getUsers = Future {
        val user: User = null
        val nodeList = Neo4jRepository.getNodesByLabel(User.label)
        val listOfUsers = nodeList.map(node => user.fromNeo4jGraph(node.getId))
        Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
      }
      onComplete(getUsers) {
        case Success(page) => complete(StatusCodes.OK, page)
        case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed to get users, Message: ${ex.getMessage}")
      }
    } ~ post {
      entity(as[User]) { user =>
        val saveUserFuture = Future {
          user.toNeo4jGraph(user)
        }
        onComplete(saveUserFuture) {
          case Success(status) => complete(StatusCodes.OK, "Successfully saved user")
          case Failure(ex) => complete(StatusCodes.BadRequest, s"Failed save user, Message: ${ex.getMessage}")
        }
      }
    }
  }*/

  private def getKeyPairInfo(userId: Long): Page[KeyPairInfo] = {
    val keysList: List[KeyPairInfo] = user.fromNeo4jGraph(userId).publicKeys
    Page(0, keysList.size, keysList.size, keysList)
  }

  private def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    val keysList: List[KeyPairInfo] = user.fromNeo4jGraph(userId).publicKeys

    keysList match {
      case keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
      case _ :: keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
      case _ => None
    }
  }

  private def addKeyPair(userId: Long, sSHKeyContentInfo: SSHKeyContentInfo): Future[Page[KeyPairInfo]] = Future {
    FileUtils.createDirectories(UserUtils.getKeyDirPath(userId))
    val keysList = sSHKeyContentInfo.keyMaterials.map {
      case (keyName: String, keyMaterial: String) =>
        logger.debug(s" (${keyName}  --> (${keyMaterial}))")
        val filePath: String = UserUtils.getKeyFilePath(userId, keyName)
        FileUtils.saveContentToFile(filePath, keyMaterial)

        val keyPairInfo = KeyPairInfo(keyName, keyMaterial, filePath, UploadedKeyPair)
        logger.debug(s" new Key Pair Info ${keyPairInfo}")
        UserUtils.addKeyPair(userId, keyPairInfo)
        keyPairInfo
    }.toList
    logger.debug(s"result from map ${keysList}")
    Page(0, keysList.size, keysList.size, keysList)
  }
}
