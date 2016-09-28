package com.imaginea.activegrid.core.models

import scala.concurrent.Future

/**
  * Created by babjik on 27/9/16.
  */
class UserRepository {
  val label = "User"

  def getUsers: Page[User] = new Page[User](Neo4jRepository.getEntityList[User](label))

  def saveUser(user: User): User = Neo4jRepository.saveEntity[User](user, label)
}
