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

  def getUsers: Future[Page[User]] = Future {
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val listOfUsers = nodeList.map(node => User.fromNeo4jGraph(Some(node.getId))).flatten

    Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers)
  }

  def saveUser(user: User): Future[Unit] = Future {
    user.toNeo4jGraph(user)
  }

  def getUser(userId: Long): Future[Option[User]] = Future {
    User.fromNeo4jGraph(Some(userId))
  }

  def getKeys(userId: Long): Future[Page[KeyPairInfo]] = Future {
    User.fromNeo4jGraph(Some(userId)) match {
      case Some(user) => Page[KeyPairInfo](user.publicKeys)
      case None => Page[KeyPairInfo](List.empty[KeyPairInfo])
    }
  }


  def getKeys(userName: String): Future[Page[KeyPairInfo]] = Future {
    logger.debug(s"Searching Users with name ${userName}")
    val maybeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(label, "username", userName)

    logger.debug(s" May be node ${maybeNode}")

    maybeNode match {
      case None => {
        Page(List.empty[KeyPairInfo])
      }
      case Some(node) => {
        User.fromNeo4jGraph(Some(node.getId)) match {
          case Some(user) => Page[KeyPairInfo](user.publicKeys)
          case None => Page[KeyPairInfo](List.empty[KeyPairInfo])
        }
      }
    }
  }

  def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    User.fromNeo4jGraph(Some(userId)) match {
      case Some(user) => {
        val keysList: List[KeyPairInfo] = user.publicKeys
        keysList match {
          case keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
          case _ :: keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
          case _ => None
        }
      }
      case None => None
    }
  }

  def getKey(userId: Long, keyId: Long): Future[Option[KeyPairInfo]] = Future {
    getKeyById(userId, keyId)
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

    val resultKeys = sSHKeyContentInfo.keyMaterials.map { case (keyName: String, keyMaterial: String) => {
      logger.debug(s" (${keyName}  --> (${keyMaterial}))")
      val filePath: String = UserUtils.getKeyFilePath(userId, keyName)
      FileUtils.saveContentToFile(filePath, keyMaterial)

      val keyPairInfo = KeyPairInfo(keyName, keyMaterial, Some(filePath), UploadedKeyPair)
      logger.debug(s" new Key Pair Info ${keyPairInfo}")
      UserUtils.addKeyPair(userId, keyPairInfo)
      keyPairInfo
    }
    }

    val keysList = resultKeys.toList
    Page(0, keysList.size, keysList.size, keysList)
  }

  def deleteUser(userId: Long): Future[Unit] = Future {
    Neo4jRepository.deleteEntity(userId)
  }
}
