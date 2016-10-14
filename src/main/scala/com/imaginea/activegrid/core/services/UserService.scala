package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.utils.FileUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by babjik on 27/9/16.
 */
class UserService(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val label = "User"
  val user: User = null

  /* User CRUD functions */
  def getUsers: Future[Option[Page[User]]] = Future {
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val listOfUsers = nodeList.map(node => user.fromNeo4jGraph(node.getId)).flatten

    Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
  }

  def saveUser(user: User): Future[Unit] = Future {
    user.toNeo4jGraph(user)
  }

  def getUser(userId: Long): Future[Option[User]] = Future {
    user.fromNeo4jGraph(userId)
  }

  def deleteUser(userId: Long): Future[Unit] = Future {
    Neo4jRepository.deleteEntity(userId)
  }

  /* KeyPairInfo CRUD functions */
  private def getKeyPairInfo(userId: Long): Page[KeyPairInfo] = {
    val keysList: List[KeyPairInfo] = user.fromNeo4jGraph(userId).map(_.publicKeys).getOrElse(List.empty)
    Page(0, keysList.size, keysList.size, keysList)
  }

  def getKeys(userId: Long): Future[Page[KeyPairInfo]] = Future {
    getKeyPairInfo(userId)
  }

  def getKeys(userName: String): Future[Page[KeyPairInfo]] = Future {
    logger.debug(s"Searching Users with name ${userName}")
    val maybeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(label, "username", userName)

    logger.debug(s" May be node ${maybeNode}")
    maybeNode match {
      case None => Page(0, 0, 0, List())
      case Some(node) => getKeyPairInfo(node.getId)
    }
  }

  def getKey(userId: Long, keyId: Long): Future[Option[KeyPairInfo]] = Future {
    getKeyById(userId, keyId)
  }

  def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    val keysList: List[KeyPairInfo] = user.fromNeo4jGraph(userId).map(_.publicKeys).getOrElse(List.empty)

    keysList match {
      case keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
      case _ :: keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
      case _ => None
    }
  }

  def deleteKey(userId: Long, keyId: Long): Future[Unit] = Future {
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

  def addKeyPair(userId: Long, sSHKeyContentInfo: SSHKeyContentInfo): Future[Page[KeyPairInfo]] = Future {

    FileUtils.createDirectories(UserUtils.getKeyDirPath(userId))

    val resultKeys = sSHKeyContentInfo.keyMaterials.map {
      case (keyName: String, keyMaterial: String) => {
        logger.debug(s" (${keyName}  --> (${keyMaterial}))")
        val filePath: String = UserUtils.getKeyFilePath(userId, keyName)
        FileUtils.saveContentToFile(filePath, keyMaterial)

        val keyPairInfo = KeyPairInfo(keyName, keyMaterial, filePath, UploadedKeyPair)
        logger.debug(s" new Key Pair Info ${keyPairInfo}")
        UserUtils.addKeyPair(userId, keyPairInfo)
        keyPairInfo
      }
    }

    logger.debug(s"result from map ${resultKeys}")

    val keysList = resultKeys.toList
    Page(0, keysList.size, keysList.size, keysList)
  }

  /*UserGroup CRUD functions*/
  /* Returning ResponseMessage type. It could be either FailureResponse or SuccessResponse
  *  SuccessResponse holding the node id of the saved entity for the future reference
  */
  def saveUserGroup(userGroup: UserGroup): Future[ResponseMessage] = Future {
    userGroup.toNeo4jGraph(userGroup) match {
      case None => FailureResponse
      case Some(node) => new SuccessResponse(node.getId.toString())
    }
  }

  /*
  * Returning the Future of Page UserGroup
  * Page content have empty list if no result found
  */
  def getUserGroups: Future[Page[UserGroup]] = Future {
    import com.imaginea.activegrid.core.models.UserGroup.RichUserGroup

    val nodeList = Neo4jRepository.getNodesByLabel(UserGroup.label)
    val userGroup: UserGroup = null;
    val listOfUserGroups: List[UserGroup] =
      nodeList.map {
        node =>
          userGroup.fromNeo4jGraph(node.getId)
      }.flatten

    Page[UserGroup](0, listOfUserGroups.size, listOfUserGroups.size, listOfUserGroups)
  }

  /*
  * Id parameter is used to fetch the node
  * Returning Option[UserGroup]
  */
  def getUserGroup(id: Long): Future[Option[UserGroup]] = Future {
    import com.imaginea.activegrid.core.models.UserGroup.RichUserGroup

    val userGroup: UserGroup = null;
    userGroup.fromNeo4jGraph(id)
  }

  def deleteUserGroup(userGroupId: Long): Future[Unit] =
    Future {
      Neo4jRepository.removeEntity[UserGroup](userGroupId)
    }

}
