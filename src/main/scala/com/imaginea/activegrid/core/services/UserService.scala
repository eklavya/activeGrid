package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models._
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
    val node = getKeyById(userId, keyId)
    logger.debug(s"node to be deleted -- ${node}")
    Some(s"Deleted key with id ${keyId}")
  }


  def addKeyPair(userId: Long, sSHKeyContentInfo: SSHKeyContentInfo): Future[Option[Page[KeyPairInfo]]] = Future {

    sSHKeyContentInfo.keyMaterials.foreach{case(keyName: String, keyMaterial: String) => {
      logger.debug(s" (${keyName}  --> (${keyMaterial}))")



    }}



    val userGraph = user.fromNeo4jGraph(userId)

    val keysList = userGraph.publicKeys
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
