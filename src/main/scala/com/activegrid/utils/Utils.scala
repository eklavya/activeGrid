package com.activegrid.utils

import scala.concurrent.Future
import akka.http.scaladsl.marshallers.sprayjson
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.Graph
import com.activegrid.entities.Software
import org.neo4j.graphdb.factory.{GraphDatabaseFactory, GraphDatabaseSettings}
import org.neo4j.shell.Session

import scala.concurrent.ExecutionContext.Implicits.global;


class Utils {

}