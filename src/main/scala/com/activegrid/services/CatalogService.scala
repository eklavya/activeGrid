package com.activegrid.services

import com.activegrid.db.DAO
import com.activegrid.model.Software

/**
  * Created by sampathr on 22/9/16.
  */
class CatalogService {

  var softwares = Vector.empty[Software]
  val daoObject = DAO

  def buildSoftware(software: Software): Int = {
    daoObject.persistEntity(software)

  }

/*
  def getSoftware(id: String): Future[Option[Software]] = Future {
    softwares.find(_.name == id)
  }*/

}
