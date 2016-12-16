package com.imaginea.activegrid.core.models

import Ports._

/**
 * Created by ranjithrajd on 6/11/16.
 */
sealed trait InstanceRole {
  val name: String
  val knownSoftwares: List[KnownSoftware]
  val ports: List[Int]

  def listensOn(port: Int): Boolean = ports.contains(port)

  def hasSoftware(software: Software): Boolean = knownSoftwares.exists(knownSoftware => knownSoftware.name.equals(software.name))
}

object InstanceRole {

  case object LoadBalancer extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Apache, KnownSoftware.Nginx)
    override val ports: List[Int] = List(port80, port443)
    override val name: String = "LB"
  }

  case object Web extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Tomcat, KnownSoftware.PServer)
    override val ports: List[Int] = List(port8080, port8008, port8181, port8443, port9080)
    override val name: String = "WEB"
  }

  case object Database extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Mysql)
    override val ports: List[Int] = List(port3306, port1521, port2483, port1433, port5432)
    override val name: String = "DATABASE"
  }

  case object Monitoring extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Graphite)
    override val ports: List[Int] = List(port6379)
    override val name: String = "MONITORING"
  }

  case object Search extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List.empty
    override val ports: List[Int] = List.empty
    override val name: String = "SEARCH"
  }

  case object MailServer extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List.empty
    override val ports: List[Int] = List.empty
    override val name: String = "MAIL_SERVER"
  }

  val allInstanceRole: List[InstanceRole] = List(LoadBalancer,Web,Database,Monitoring,Search,MailServer)

  def hasSoftware(software: Software): Option[InstanceRole] = allInstanceRole.filter( role => role.hasSoftware(software)) match {
    case Nil => None
    case x :: xs => Some(x)
  }
  def listensOnPort(portRange: PortRange):Set[InstanceRole] =
    allInstanceRole.filter( role => role.ports.exists( port=> portRange.containsPort(port))).toSet
}

object Ports{
  val port80 = 80
  val port443 = 443

  val port8080 = 8080
  val port8008 = 8008
  val port8181 = 8181
  val port8443 = 8443
  val port9080 = 9080

  val port3306 = 3306
  val port1521 = 1521
  val port2483 = 2483
  val port1433 = 1433
  val port5432 = 5432

  val port6379 = 6379

}