package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.CmdExecUtils

/**
  * Created by nagulmeeras on 12/12/16.
  */
class GrepCommand {
  val caseInsensitive: Boolean = false
  val recursive: Boolean = false
  val inputs = List.empty[String]

  def execute(commandExecutionContext: CommandExecutionContext, inputContexts : List[Line]): List[Line] = {
    if (inputs.nonEmpty) {
      val pattern = inputs.head
      val providerContext = inputs.size match {
        case size if size > 1 => Some(inputs(1))
        case _ => None
      }
      providerContext match {
        case Some(providerCtx) =>
          val targetContext = CmdExecUtils.getDervivedContext(providerCtx, commandExecutionContext)
          targetContext match {
            case Some(ctx) =>
              val contextObj = ctx.contextObject
              val contextType = ctx.contextType
              contextType match {
                case USER_HOME => if (recursive) {
                  searchAllSitesAndInstances(pattern)
                } else {
                  searchSites(pattern)
                }
                case SITE =>
                  val site = contextObj.asInstanceOf[Site1]
                  if (recursive) {
                    searchInstance(site, pattern)
                  } else {
                    searchSites(pattern)
                  }
                case _ =>
                  val site = contextObj.asInstanceOf[Site1]
                  searchInstance(site, pattern)
              }
          }
        case None => inputContexts.flatMap { line =>
          line.values.flatMap { lineValue =>
            lineValue match {
              case value if value.contains(pattern) || value.matches(pattern) => Some(line)
              case _ => None
            }
          }
        }
      }
    } else {
      throw new Exception("Command execution exception")
    }
  }

  def searchInstance(site: Site1, pattern: String): List[Line] = {
    site.instances.flatMap { instance =>
      val instanceName = instance.name
      val dNSName = instance.publicDnsName match {
        case Some(dnsName) => dnsName
        case None => ""
      }
      if (instanceName.contains(pattern) || instanceName.matches(pattern)
        || dNSName.contains(pattern) || dNSName.matches(pattern)) {
        Some(Line(InstanceViewHelper.fetchInstance(instance, SUMMARY), DIRECTORY, None))
      } else {
        None
      }
    }
  }

  def searchSites(pattern: String): List[Line] = {
    val siteNodes = Neo4jRepository.getNodesByLabel(Site1.label)
    val sites = siteNodes.flatMap(node => Site1.fromNeo4jGraph(node.getId))
    sites.flatMap { site =>
      if (site.siteName.matches(pattern) || site.siteName.contains(pattern)) {
        Some(Line(SiteViewFilter.fetchSite(site, SUMMARY), DIRECTORY, None))
      } else {
        None
      }
    }
  }

  def searchAllSitesAndInstances(pattern: String): List[Line] = {
    val siteNodes = Neo4jRepository.getNodesByLabel(Site1.label)
    val sites = siteNodes.flatMap(node => Site1.fromNeo4jGraph(node.getId))
    sites.flatMap { site =>
      val list = searchInstance(site, pattern)
      if (site.siteName.matches(pattern) || site.siteName.contains(pattern)) {
        Line(SiteViewFilter.fetchSite(site, SUMMARY), DIRECTORY, None) :: list
      } else {
        None
      }

    }
  }
}
