package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/11/16.
  */

case class EsQueryField(key: String, value: String)

case class EsSearchQuery(index: String,
                         types: List[String],
                         outputField: String,
                         queryString: String,
                         queryFields: List[EsQueryField],
                         queryType: EsQueryType)