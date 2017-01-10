package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 4/1/17.
  */
sealed trait ScopeType {
  val scopeType: String
}

object ScopeType {

  case object Node extends ScopeType {
    override val scopeType: String = "Node"
  }

  case object NodeGroup extends ScopeType {
    override val scopeType: String = "NodeGroup"
  }

  case object Site extends ScopeType {
    override val scopeType: String = "Site"
  }

  case object IPRange extends ScopeType {
    override val scopeType: String = "IPRange"
  }

  case object Inventory extends ScopeType {
    override val scopeType: String = "Inventory"
  }

  def toScopeType(scopeType: String): ScopeType = {
    scopeType match {
      case "Node" => Node
      case "NodeGroup" => NodeGroup
      case "Site" => Site
      case "IPRange" => IPRange
      case "Inventory" => Inventory
    }
  }

  def values: List[ScopeType] = List(Node, NodeGroup, Site, IPRange, Inventory)
}