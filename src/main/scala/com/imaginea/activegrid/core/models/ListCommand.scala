package com.imaginea.activegrid.core.models

import com.beust.jcommander.Parameter
import com.imaginea.activegrid.core.utils.CmdExecUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 07/12/16.
  */
object ListCommand {

  @Parameter(names = Array("-l"), description = "use long listing format")
  val list: Boolean = false

  @Parameter(description = "the current context to execute command on")
  var newContext: java.util.List[String] = new java.util.ArrayList()

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def execute(commandExecutionContext: CommandExecutionContext): List[String] = {
    val targetContext = newContext.toList match {
      case context if context.nonEmpty && context.size == 1 =>
        val derivedContext = newContext.head
        CmdExecUtils.getDervivedContext(derivedContext, commandExecutionContext)
      case _ =>
        Some(CommandExecutionContext(commandExecutionContext.contextName,
          commandExecutionContext.contextType,
          commandExecutionContext.contextObject,
          commandExecutionContext.parentContext))
    }
    targetContext match {
      case Some(context) =>
        val viewLevel = list match {
          case true => DETAILED
          case false => SUMMARY
        }
        context.contextType match {
          case USER_HOME =>
            val sites = Neo4jRepository.getNodesByLabel(Site1.label).flatMap(node => Site1.fromNeo4jGraph(node.getId))
            sites.flatMap { site =>
              SiteViewFilter.fetchSite(site, viewLevel)
            }
          case SITE =>
            val mayBeSite = CmdExecUtils.getSiteByName(context.contextName)
            mayBeSite match {
              case Some(site) =>
                val instances = site.instances
                instances.flatMap { instance =>
                  InstanceViewHelper.fetchInstance(instance, viewLevel)
                }
              case None =>
                logger.debug(s"No Site entity found with name : ${context.contextName}")
                List.empty[String]
            }
        }
      case None =>
        logger.warn("No target context is found")
        throw new Exception("No context Found")
    }
  }
}
