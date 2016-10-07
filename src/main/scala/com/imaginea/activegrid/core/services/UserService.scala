package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.utils.FileUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by babjik on 27/9/16.
  */
class UserService (implicit val executionContext: ExecutionContext){
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val label = "User"
  val user: User = null

  def getUsers: Future[Option[Page[User]]] = Future {
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val listOfUsers = nodeList.map(node => user.fromNeo4jGraph(node.getId))

    Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
  }

  def saveUser(user: User): Future[Option[String]] = Future {
    user.toNeo4jGraph(user)
    Some("Successfull")
  }

  def getUser(userId: Long): Future[Option[User]] = Future {
    Some(user.fromNeo4jGraph(userId))
  }

  def getKeys(userId: Long): Future[Option[Page[KeyPairInfo]]] = Future {
    val keysList = user.fromNeo4jGraph(userId).publicKeys
    Some(Page(0, keysList.size, keysList.size, keysList))
  }


  def getKeys(userName: String): Future[Option[Page[KeyPairInfo]]] = Future {
    logger.debug(s"Searching Users with name ${userName}")
    val maybeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(label, "username", userName)

    logger.debug(s" May be node ${maybeNode}")

    maybeNode match {
      case None => {
        Some(Page(0,0,0, List()))
      }
      case Some(node) => {
        val keysList = user.fromNeo4jGraph(node.getId).publicKeys
        Some(Page(0, keysList.size, keysList.size, keysList))
      }
    }

  }

  def getKeyById (userId: Long, keyId: Long): Option[KeyPairInfo] = {
    val keysList: List[KeyPairInfo] = user.fromNeo4jGraph(userId).publicKeys

    keysList match {
    case keyInfo::_ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
    case _::keyInfo::_ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
    case _ => None
  }
  }
  def getKey (userId: Long, keyId: Long): Future[Option[KeyPairInfo]] = Future {
    getKeyById(userId, keyId)
  }


  def deleteKey(userId: Long, keyId: Long): Future[Option[String]] = Future {
    logger.debug(s"Deleting Key[${keyId}] of User[${userId}] ")
    val key = getKeyById(userId, keyId)

    key match {
      case Some(keyPairInfo) => {
        val status = Neo4jRepository.deleteChildNode(keyId)
        status match {
          case Some(true) => Some("Deleted Successfully")
          case _ => Some("Failed to delete node")
        }
      }
      case None => Some(s"No key pair found with id ${keyId}")
    }
  }


  def addKeyPair(userId: Long, sSHKeyContentInfo: SSHKeyContentInfo): Future[Option[Page[KeyPairInfo]]] = Future {

    FileUtils.createDirectories(UserUtils.getKeyDirPath(userId))

    val resultKeys = sSHKeyContentInfo.keyMaterials.map{case(keyName: String, keyMaterial: String) => {
      logger.debug(s" (${keyName}  --> (${keyMaterial}))")
      val filePath: String = UserUtils.getKeyFilePath(userId, keyName)
      FileUtils.saveContentToFile(filePath, keyMaterial)

      val keyPairInfo =  KeyPairInfo(keyName, keyMaterial, filePath, KeyPairStatus.UPLOADED)
      logger.debug(s" new Key Pair Info ${keyPairInfo}")
      UserUtils.addKeyPair(userId, keyPairInfo)
      keyPairInfo
    }}

    logger.debug(s"result from map ${resultKeys}")

    val userGraph = user.fromNeo4jGraph(userId)

    val keysList = resultKeys.toList
    Some(Page(0, keysList.size, keysList.size, keysList))
  }

  def deleteUser(userId: Long): Future[Option[String]] = Future {
    try {
      Neo4jRepository.deleteEntity(userId)
      Some("Successfull")
    } catch {
      case e: NotFoundException => {
        Some(e.getMessage)
      }
    }

  }

  def saveUserGroup(userGroup: UserGroup): Future[Option[String]] = Future{
    userGroup.toNeo4jGraph(userGroup)
    Some("Successfull")
  }
}
