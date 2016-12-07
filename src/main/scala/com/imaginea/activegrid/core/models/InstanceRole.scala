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

  case object LoadBalancer extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Apache, KnownSoftware.Nginx)
    override val ports: List[Int] = List(80, 443)
    override val name: String = "LB"
  }

  case object Web extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Tomcat, KnownSoftware.PServer)
    override val ports: List[Int] = List(8080, 8008, 8181, 8443, 9080)
    override val name: String = "WEB"
  }

  case object Database extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Mysql)
    override val ports: List[Int] = List(3306, 1521, 2483, 1433, 5432)
    override val name: String = "DATABASE"
  }

  case object Monitoring extends InstanceRole {
    override val knownSoftwares: List[KnownSoftware] = List(KnownSoftware.Graphite)
    override val ports: List[Int] = List(6379)
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
