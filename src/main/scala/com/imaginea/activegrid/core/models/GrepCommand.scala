package com.imaginea.activegrid.core.models

import com.beust.jcommander.Parameter
import com.imaginea.activegrid.core.utils.CmdExecUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._ //scalastyle:ignore underscore.import

/**
  * Created by nagulmeeras on 12/12/16.
  */
class GrepCommand extends Command {
  @Parameter(names = Array("-i"), description = "is the pattern match case insensitive")
  var caseInsensitive: Boolean = false

  @Parameter(names = Array("-r", "-R"), description = "search be done recursively")
  var recursive: Boolean = false

  @Parameter(description = "Pattern and file inputs", variableArity = true, arity = 2)
  var inputs: java.util.List[String] = new java.util.ArrayList()

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  override def execute(commandExecutionContext: CommandExecutionContext, inputContexts: List[Line]): CommandResult = {
    if (inputs.nonEmpty) {
      val pattern = inputs.head
      val providerContext = if (inputs.size > 1) {
        Some(inputs(1))
      } else {
        logger.debug("More number of inputs found for Grep command")
        None
      }
      val lines = providerContext match {
        case Some(providerCtx) =>
          val targetContext = CmdExecUtils.getDervivedContext(providerCtx, commandExecutionContext)
          targetContext match {
            case Some(ctx) =>
              val contextObj = ctx.contextObject
              val contextType = ctx.contextType
              contextType match {
                case USER_HOME =>
                  if (recursive) {
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
        case None => inputContexts.filter { line =>
          line.values.exists(lineValue => lineValue.contains(pattern) || lineValue.matches(pattern))
        }
      }
      CommandResult(lines, Some(commandExecutionContext))
    } else {
      logger.warn("No input parameter found")
      throw new Exception("No input options found for command like File or Directory")
    }
  }

  def searchInstance(site: Site1, pattern: String): List[Line] = {
    site.instances.flatMap { instance =>
      val instanceName = instance.name
      val dNSName = instance.publicDnsName
      if (instanceName.contains(pattern) || instanceName.matches(pattern)
        || dNSName.exists(dnsName => dnsName.contains(pattern) || dnsName.matches(pattern))) {
        Some(Line(InstanceViewHelper.fetchInstance(instance, SUMMARY), DIRECTORY, None))
      } else {
        logger.debug(s"No matches found in Instances with $pattern")
        None
      }

    }
  }

  def searchSites(pattern: String): List[Line] = {
    val siteNodes = Neo4jRepository.getNodesByLabel(Site1.label)
    val sites = siteNodes.flatMap(node => Site1.fromNeo4jGraph(node.getId))
    sites.flatMap { site =>
      matchWithSiteName(site, pattern)
    }
  }

  def searchAllSitesAndInstances(pattern: String): List[Line] = {
    val siteNodes = Neo4jRepository.getNodesByLabel(Site1.label)
    val sites = siteNodes.flatMap(node => Site1.fromNeo4jGraph(node.getId))
    sites.flatMap { site =>
      val list = searchInstance(site, pattern)
      matchWithSiteName(site, pattern)
    }
  }

  def matchWithSiteName(site: Site1, pattern: String): Option[Line] = {
    if (site.siteName.matches(pattern) || site.siteName.contains(pattern)) {
      Some(Line(SiteViewFilter.fetchSite(site, SUMMARY), DIRECTORY, None))
    } else {
      logger.debug(s"No matches found in Site with $pattern")
      None
    }
  }
}
