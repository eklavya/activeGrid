package com.imaginea.activegrid.core.services

import akka.http.scaladsl.model.Multipart
import com.imaginea.activegrid.core.models.{KeyPairInfo, Neo4jRepository, Page}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConversions._

/**
  * Created by babjik on 13/10/16.
  */
class KeyPairService(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val label = "KeyPairInfo"
  val keyPairInfo: KeyPairInfo = null

  def getKeyPairs: Future[Page[KeyPairInfo]] = Future {
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val listOfKeys = nodeList.map(node => keyPairInfo.fromNeo4jGraph(node.getId))
    Page[KeyPairInfo](0, listOfKeys.size, listOfKeys.size, listOfKeys)
  }

  def getKey(keyId: Long): Future[Option[KeyPairInfo]] = Future {
    val mayBeKey = Neo4jRepository.findNodeByLabelAndId(label, keyId)
    mayBeKey match {
      case Some(key) => Some(keyPairInfo.fromNeo4jGraph(keyId))
      case None => throw new Exception(s"Unable to locate KeyPair with Id $keyId")
    }
  }

  def deleteKeyById(keyId: Long) =  Future {
    val mayBeKey = Neo4jRepository.findNodeByLabelAndId(label, keyId)
    mayBeKey match {
      case Some(key) => Neo4jRepository.deleteChildNode(keyId)
      case None => throw new Exception(s"Unable to locate KeyPair with Id $keyId")
    }
  }

  def uploadKeyPairs(formData: Multipart.FormData): Future[Page[KeyPairInfo]] = Future {
    logger.debug(s"trying to upload keys from formData ")
    val bodyParts = formData.getParts()



    Page[KeyPairInfo](0,0,0,List.empty[KeyPairInfo])
  }
}
