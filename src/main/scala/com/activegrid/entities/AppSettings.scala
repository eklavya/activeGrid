package com.activegrid.entities

import java.util.Date


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(id: Int,
                       createdAt: Date,
                       createdBy: String,
                       lastUpdatedAt: Date,
                       lastUpdatedBy: String,
                       settings: List[Setting])
