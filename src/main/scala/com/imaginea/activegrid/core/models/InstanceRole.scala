package com.imaginea.activegrid.core.models

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
  import Ports._
  case object LoadBalancer extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Apache, KnownSoftware.Nginx)
    override val ports: List[Int] = List(Port80, Port443)
    override val name: String = "LB"
  }

  case object Web extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Tomcat, KnownSoftware.PServer)
    override val ports: List[Int] = List(Port8080, Port8008, Port8181, Port8443, Port9080)
    override val name: String = "WEB"
  }

  case object Database extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Mysql)
    override val ports: List[Int] = List(Port3306, Port1521, Port2483, Port1433, Port5432)
    override val name: String = "DATABASE"
  }

  case object Monitoring extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Graphite)
    override val ports: List[Int] = List(Port6379)
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
  val Port80 = 80
  val Port443 = 443

  val Port8080 = 8080
  val Port8008 = 8008
  val Port8181 = 8181
  val Port8443 = 8443
  val Port9080 = 9080

  val Port3306 = 3306
  val Port1521 = 1521
  val Port2483 = 2483
  val Port1433 = 1433
  val Port5432 = 5432

  val Port6379 = 6379

}