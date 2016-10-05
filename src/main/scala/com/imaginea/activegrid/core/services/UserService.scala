package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models._
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by babjik on 27/9/16.
  */
class UserService (implicit val executionContext: ExecutionContext){
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val label = "User"


  def getUsers: Future[Option[Page[User]]] = Future {
    import com.imaginea.activegrid.core.models.Implicits.RichUser
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val user: User = null
    val listOfUsers: List[User] = nodeList.map(node => user.fromGraph(node.getId))

    Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
  }

  def saveUser(user: User): Future[Option[String]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    user.toGraph(user)
    Some("Successfull")
  }
  def saveUserGroup(userGroup: UserGroup): Future[Option[String]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    userGroup.toGraph(userGroup)
    Some("User group created !")
  }
  def getUserGroups: Future[Option[Page[UserGroup]]] = Future {
    import com.imaginea.activegrid.core.models.Implicits.RichUserGroup

    val nodeList = Neo4jRepository.getNodesByLabel(UserGroupProtocol.labelUserGroup)
    val userGroup : UserGroup = null;
    val listOfUserGroups : List[Option[UserGroup]] = nodeList.map(node => userGroup.fromGraph(node.getId))

    Some(Page[UserGroup](0, listOfUserGroups.size, listOfUserGroups.size, listOfUserGroups.map(_.get)))
  }
}
