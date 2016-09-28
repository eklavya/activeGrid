package com.imaginea.activegrid.core.services

import com.imaginea.activegrid.core.models.{Page, User, UserRepository}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by babjik on 27/9/16.
  */
class UserService (implicit val executionContext: ExecutionContext){
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val userRepository: UserRepository = new UserRepository

  def getUsers: Future[Option[Page[User]]] = Future {Some(userRepository.getUsers)}

  def saveUser(user: User): Future[Option[User]] = Future {Some(userRepository.saveUser(user))}
}
