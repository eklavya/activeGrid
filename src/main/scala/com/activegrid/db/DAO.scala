package com.activegrid.db

import com.activegrid.model.Software
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase, StatementResult}

/**
  * Created by sampathr on 22/9/16.
  */
object DAO {

  def persistEntity(software: Software): Int = {

    val driver = GraphDatabase.driver("bolt://localhost/7474", AuthTokens.basic("neo4j", "pramati123"))
    val session = driver.session
    val script = s"CREATE (user:Software {version:'${software.version}',name:'${software.name}',provider:'${software.provider}'," +
      s"downloadURL:'${software.downloadURL}',port:'${software.port}',processName:'${software.processName}'," +
      s"discoverApplications:'${software.discoverApplications}'})"
    val result: StatementResult = session.run(script)
    session.close()
    driver.close()
    result.consume().counters().nodesCreated()
  }
}