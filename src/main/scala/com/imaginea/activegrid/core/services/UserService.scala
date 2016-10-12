package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models._
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.NotFoundException
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by babjik on 27/9/16.
 */
class UserService(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val label = "User"
  val user: User = null


  def getUsers: Future[Option[Page[User]]] = Future {
    val nodeList = Neo4jRepository.getNodesByLabel(label)

    val listOfUsers: List[User] = nodeList.map(node => user.fromNeo4jGraph(node.getId)).flatten

    Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
  }

  def saveUser(user: User): Future[Option[String]] = Future {
    user.toNeo4jGraph(user)
    Some("Successful")
  }

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

    val nodeList = Neo4jRepository.getNodesByLabel(UserGroupProtocol.labelUserGroup)
    val userGroup: UserGroup = null;
    val listOfUserGroups: List[UserGroup] =
      nodeList.map {
        node =>
          val a = userGroup.fromNeo4jGraph(node.getId)
          println("Mapping = " + a)
          a
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
      Neo4jRepository.removeEntity[UserGroupProxy](userGroupId)
    }

  def getUser(userId: Long): Future[Option[User]] = Future {
    user.fromNeo4jGraph(userId)
  }

  def getKeys(userId: Long): Future[Page[KeyPairInfo]] = Future {
    val keysList = user.fromNeo4jGraph(userId).map(_.publicKeys).getOrElse(List.empty)
    Page(0, keysList.size, keysList.size, keysList)
  }


  def getKeys(userName: String): Future[Option[Page[KeyPairInfo]]] = Future {
    logger.debug(s"Searching Users with name ${userName}")
    val maybeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(label, "username", userName)

    logger.debug(s" May be node ${maybeNode}")

    maybeNode match {
      case None => {
        Some(Page(0, 0, 0, List()))
      }
      case Some(node) => {
        val keysList = user.fromNeo4jGraph(node.getId).map(_.publicKeys).getOrElse(List.empty)
        Some(Page(0, keysList.size, keysList.size, keysList))
      }
    }

  }

  def getKey(userId: Long, keyId: Long): Future[Option[KeyPairInfo]] = Future {
    getKeyById(userId, keyId)
  }

  def deleteKey(userId: Long, keyId: Long): Future[Option[String]] = Future {
    logger.debug(s"Deleting Key[${keyId}] of User[${userId}] ")
    val node = getKeyById(userId, keyId)
    logger.debug(s"node to be deleted -- ${node}")
    Some(s"Deleted key with id ${keyId}")
  }

  def getKeyById(userId: Long, keyId: Long): Option[KeyPairInfo] = {
    val keysList: List[KeyPairInfo] = user.fromNeo4jGraph(userId).map(_.publicKeys).getOrElse(List.empty)

    keysList match {
      case keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
      case _ :: keyInfo :: _ if keyInfo.id.get.equals(keyId) => Some(keyInfo)
      case _ => None
    }
  }

  def addKeyPair(userId: Long, sSHKeyContentInfo: SSHKeyContentInfo): Future[Option[Page[KeyPairInfo]]] = Future {

    val keyMaterial = sSHKeyContentInfo.keyMaterials

    keyMaterial.foreach { case (key: String, value: String) => {
      logger.debug(s" (${key}  --> (${value}))")
    }
    }

    //TODO: need to add code to write data to file

    val keysList = user.fromNeo4jGraph(userId).map(_.publicKeys).getOrElse(List.empty)
    Some(Page(0, keysList.size, keysList.size, keysList))
  }

  def deleteUser(userId: Long): Future[Option[String]] = Future {
    try {
      Neo4jRepository.deleteEntity(userId)
      Some("Successful")
    } catch {
      case e: NotFoundException => {
        Some(e.getMessage)
      }
    }
  }

  def createACL(siteACL: SiteACL): Future[SiteACL] = Future {
    //TODO : Persistence impl
    siteACL
  }
}