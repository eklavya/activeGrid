package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models.{Neo4jRepository, Page, User}
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
    val listOfUsers = nodeList.map(node => user.fromGraph(node.getId))

    Some(Page[User](0, listOfUsers.size, listOfUsers.size, listOfUsers))
  }

  def saveUser(user: User): Future[Option[String]] = Future {
    import com.imaginea.activegrid.core.models.Implicits._
    user.toGraph(user)
    Some("Successfull")
  }
}
