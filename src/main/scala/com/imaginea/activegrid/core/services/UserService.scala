package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models._
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, NotFoundException}
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
    import com.imaginea.activegrid.core.models.Implicits.RichUser
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val listOfUsers = nodeList.map(node => user.fromGraph(node.getId))

    Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
  }

  def saveUser(user: User): Future[Option[String]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    user.toGraph(user)
    Some("Successfull")
  }

  def getUser(userId: Long): Future[Option[User]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    Some(user.fromGraph(userId))
  }

  def getKeys(userId: Long): Future[Option[Page[KeyPairInfo]]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    val keysList = user.fromGraph(userId).publicKeys
    Some(Page(0, keysList.size, keysList.size, keysList))
  }

  def addKeyPair(userId: Long): Future[Option[Page[KeyPairInfo]]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    val keysList = user.fromGraph(userId).publicKeys
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
    import com.imaginea.activegrid.core.models.Implicits._
    userGroup.toGraph(userGroup)
    Some("Successfull")
  }
}
