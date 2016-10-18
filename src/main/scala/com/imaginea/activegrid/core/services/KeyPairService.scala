package com.imaginea.activegrid.core.services

import akka.http.scaladsl.model.Multipart
import akka.http.scaladsl.model.Multipart.FormData
import com.imaginea.activegrid.core.models._
import com.imaginea.activegrid.core.utils.{Constants, FileUtils}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by babjik on 13/10/16.
  */
class KeyPairService(implicit val executionContext: ExecutionContext) {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val label = "KeyPairInfo"
  val userNameLabel = "userName"
  val passPhaseLabel = "passPhase"

  def getKeyPairs: Future[Page[KeyPairInfo]] = Future {
    val nodeList = Neo4jRepository.getNodesByLabel(label)
    val listOfKeys = nodeList.flatMap(node => KeyPairInfo.fromNeo4jGraph(Some(node.getId)))
    Page[KeyPairInfo](0, listOfKeys.size, listOfKeys.size, listOfKeys)
  }

  def getKey(keyId: Long): Future[Option[KeyPairInfo]] = Future {
    val mayBeBode = Neo4jRepository.findNodeByLabelAndId(label, keyId)
    mayBeBode match {
      case Some(node) => KeyPairInfo.fromNeo4jGraph(Some(node.getId))
      case None => None
    }
  }

  def deleteKeyById(keyId: Long) = Future {
    Neo4jRepository.deleteChildNode(keyId)
  }

  def uploadKeyPairs(formData: Multipart.FormData): Future[Page[KeyPairInfo]] = Future {
    val dataMap = formData.asInstanceOf[FormData.Strict].strictParts.map(strict => {
      val name = strict.getName()
      val value = strict.entity.getData().decodeString("UTF-8")
      val optionalFileName = strict.getFilename()
      logger.debug(s"--- $name  -- $value -- $optionalFileName")

      if (optionalFileName.equals(java.util.Optional.empty)) {
        logger.debug(s" simple field")
        name match {
          case "userName" | "passPhase" => (name, value)
          case _ => (new String, new String)
        }
      } else {
        logger.debug(s"reading from the file $optionalFileName")
        (name, value)
      }

    }).filter{case (k, v) => !k.isEmpty}.toMap[String, String]

    val sshKeyContentInfo: SSHKeyContentInfo = SSHKeyContentInfo(dataMap)
    logger.debug(s"ssh info   - $sshKeyContentInfo")
    logger.debug(s"Data Map --- $dataMap")
    val addedKeyPairs: List[KeyPairInfo] = dataMap.map { case (keyName, keyMaterial) =>
      if (!userNameLabel.equalsIgnoreCase(keyName) && !passPhaseLabel.equalsIgnoreCase(keyName)) {
        val keyPairInfo = getOrCreateKeyPair(keyName, keyMaterial, None, UploadedKeyPair, dataMap.get(userNameLabel), dataMap.get(passPhaseLabel))
        val mayBeNode = saveKeyPair(keyPairInfo)
        mayBeNode match {
          case Some(key) => key
          case _ => // do nothing
        }
      }
    }.toList.collect { case x: KeyPairInfo => x }

    Page[KeyPairInfo](addedKeyPairs)
  }


  def getOrCreateKeyPair(keyName: String, keyMaterial: String, keyFilePath: Option[String], status: KeyPairStatus, defaultUser: Option[String], passPhase: Option[String]): KeyPairInfo = {
    val mayBeKeyPair = getKeyPair(keyName)

    mayBeKeyPair match {
      case Some(keyPairInfo) =>
        KeyPairInfo(keyPairInfo.id, keyName, keyPairInfo.keyFingerprint, keyMaterial, if (keyFilePath.isEmpty) keyPairInfo.filePath else keyFilePath, status, if (defaultUser.isEmpty) keyPairInfo.defaultUser else defaultUser, if (passPhase.isEmpty) keyPairInfo.passPhrase else passPhase)
      case None => KeyPairInfo(keyName, keyMaterial, keyFilePath, status)
    }
  }


  def getKeyPair(keyName: String): Option[KeyPairInfo] = {
    val mayBeNode = Neo4jRepository.getSingleNodeByLabelAndProperty(label, "keyName", keyName)
    mayBeNode match {
      case Some(node) => KeyPairInfo.fromNeo4jGraph(Some(node.getId))
      case None => None
    }
  }

  def saveKeyPair(keyPairInfo: KeyPairInfo): Option[KeyPairInfo] = {
    val filePath = getKeyFilePath(keyPairInfo.keyName)
    try {
      FileUtils.createDirectories(getKeyFilesDir)
      FileUtils.saveContentToFile(filePath, keyPairInfo.keyMaterial)
      // TODO: change permissions to 600
    } catch {
      case e: Throwable => logger.error(e.getMessage, e)
    }
    val mayBeNode = keyPairInfo.toNeo4jGraph(KeyPairInfo(keyPairInfo.id, keyPairInfo.keyName, keyPairInfo.keyFingerprint, keyPairInfo.keyMaterial, Some(filePath), keyPairInfo.status, keyPairInfo.defaultUser, keyPairInfo.passPhrase))
    mayBeNode match {
      case Some(node) => KeyPairInfo.fromNeo4jGraph(Some(node.getId))
      case None => None
    }
  }


  def getKeyFilesDir: String = s"${Constants.getTempDirectoryLocation}${Constants.FILE_SEPARATOR}"

  def getKeyFilePath(keyName: String) = s"$getKeyFilesDir$keyName.pem"
}
