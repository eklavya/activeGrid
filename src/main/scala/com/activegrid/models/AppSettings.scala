package com.activegrid.models


import org.joda.time.DateTime


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(id: Int,
                       createdAt: DateTime,
                       createdBy: String,
                       lastUpdatedAt: DateTime,
                       lastUpdatedBy: String) extends BaseEntity with Neo4jRep[AppSettings]
