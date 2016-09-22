package com.activegrid.graphdb

import com.activegrid.model.ImageInfo

import org.neo4j.driver.v1._

//imageId:String,imageType:String,lastUpdatedBy:String
/**
  * Created by shareefn on 22/9/16.
  */
class DBExecutor {

  val driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "pramati123"))
  val session = driver.session

  def getEntities(): List[ImageInfo] = {



    session.close()
    driver.close()

    List(ImageInfo("1","2","3"))
  }

  def persistEntity(imageInfo: ImageInfo): ImageInfo = {

    var cypher: String =
      f"""CREATE (imageNode:ImageInfo { imageId:"${imageInfo.imageId}",
                                                     imageType:"${imageInfo.imageType}",
                                                     lastUpdatedBy:"${imageInfo.lastUpdatedBy}" })"""


    val executeQuery: StatementResult = session.run(cypher)


    session.close()
    driver.close()

    return imageInfo
  }

}
