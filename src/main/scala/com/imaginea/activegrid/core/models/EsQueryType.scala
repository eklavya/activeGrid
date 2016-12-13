package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/11/16.
  */
sealed trait EsQueryType {
  def queryType: String
}

object EsQueryType {

  case object AND extends EsQueryType {
    override def queryType: String = "AND"
  }

  case object OR extends EsQueryType {
    override def queryType: String = "OR"
  }

  case object TEXT extends EsQueryType {
    override def queryType: String = "TEXT"
  }

  case object FACET extends EsQueryType {
    override def queryType: String = "FACET"
  }

  def convertToQueryType(queryType: String): EsQueryType = {
    queryType match {
      case "AND" => AND
      case "OR" => OR
      case "TEXT" => TEXT
      case "FACET" => FACET
    }
  }
}