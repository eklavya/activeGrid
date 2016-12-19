package com.imaginea.activegrid.core.models

import com.beust.jcommander.Parameter
import com.imaginea.activegrid.core.utils.CmdExecUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * Created by nagulmeeras on 07/12/16.
  */
class ListCommand extends Command {

  @Parameter(names = Array("-l"), description = "use long listing format")
  var list: Boolean = false

  @Parameter(description = "the current context to execute command on")
  var newContext: java.util.List[String] = new java.util.ArrayList()

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  override def execute(commandExecutionContext: CommandExecutionContext, lines: List[Line]): CommandResult = {
    val targetContext = if (newContext.toList.size == 1) {
      val derivedContext = newContext(0)
      CmdExecUtils.getDervivedContext(derivedContext, commandExecutionContext)
    } else {
      Some(CommandExecutionContext(commandExecutionContext.contextName,
        commandExecutionContext.contextType,
        commandExecutionContext.contextObject,
        commandExecutionContext.parentContext))
    }
    val lines = targetContext match {
      case Some(context) =>
        val viewLevel = list match {
          case true => DETAILED
          case false => SUMMARY
        }
        context.contextType match {
          case USER_HOME =>
            val sites = Neo4jRepository.getNodesByLabel(Site1.label).flatMap(node => Site1.fromNeo4jGraph(node.getId))
            sites.map { site =>
              Line(SiteViewFilter.fetchSite(site, viewLevel), DIRECTORY, None)
            }
          case SITE =>
            val mayBeSite = CmdExecUtils.getSiteByName(context.contextName)
            mayBeSite match {
              case Some(site) =>
                val instances = site.instances
                instances.map { instance =>
                  Line(InstanceViewHelper.fetchInstance(instance, viewLevel), DIRECTORY, None)
                }
              case None =>
                logger.debug(s"No Site entity found with name : ${context.contextName}")
                List.empty[Line]
            }
        }
      case None =>
        logger.warn("No target context is found")
        throw new Exception("No context Found")
    }
    CommandResult(lines, Option(commandExecutionContext))
  }

}
