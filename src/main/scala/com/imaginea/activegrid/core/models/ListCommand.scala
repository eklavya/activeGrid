package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.CmdExecUtils

/**
  * Created by nagulmeeras on 07/12/16.
  */
class ListCommand {

  var list: Boolean = false
  var newContext: List[String] = List()

  def execute(commandExecutionContext: CommandExecutionContext): List[String] = {
    val targetContext = newContext match {
      case ctx if ctx.nonEmpty && ctx.size == 1 =>
        val derivedCtx = newContext.head
        CmdExecUtils.getDervivedContext(derivedCtx, commandExecutionContext)
      case _ => Some(CommandExecutionContext(commandExecutionContext.contextName,
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
              case None => List.empty[String]
            }
        }
      case None => throw new Exception("No context Found")
    }
  }
}
