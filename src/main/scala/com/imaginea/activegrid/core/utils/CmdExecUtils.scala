package com.imaginea.activegrid.core.utils

import com.imaginea.activegrid.core.models._

import scala.util.control.Breaks

/**
  * Created by nagulmeeras on 07/12/16.
  */
object CmdExecUtils {
  def getDervivedContext(context: String, cmdExecutionContext: CommandExecutionContext): Option[CommandExecutionContext] = {
    val targetContext = context.trim.replaceAll("\\\\", "/")
    val paths = targetContext.split("/")

    var excontext = targetContext.startsWith("/") match {
      case true =>
        if (cmdExecutionContext.contextType.contextType.equals(USER_HOME.contextType)) {
          Some(cmdExecutionContext)
        } else {
          var context = cmdExecutionContext.parentContext
          val br = new Breaks
          while (context.nonEmpty) {
            if (context.exists(cts => cts.contextType.contextType.equals(USER_HOME.contextType))) {
              br.break()
            } else {
              context = context.flatMap(ctx => ctx.parentContext)
            }
          }
          context
        }

      case false => Some(CommandExecutionContext(cmdExecutionContext.contextName,
        cmdExecutionContext.contextType,
        cmdExecutionContext.contextObject,
        cmdExecutionContext.parentContext,
        Array(), 1L, None))
    }
    paths.foreach { path =>
      path match {
        case p if p.isEmpty || p.equals("/") || p.equals(".") =>
        case p if p.equals("..") =>
          excontext = excontext.flatMap(_.parentContext)
          if (excontext.isEmpty) throw new Exception("No such context to navigate")
        case _ =>
          excontext.foreach { ctx =>
            val contextType = getContextType(ctx)
            val contextObject = getContextObject(ctx)
            excontext = Some(CommandExecutionContext.apply(path, contextType, contextObject, excontext))
          }
      }
    }
    excontext
  }

  def getContextType(cmdExecContext: CommandExecutionContext): ContextType = {
    val contextType = cmdExecContext.contextType
    contextType match {
      case USER_HOME => SITE
      case SITE => INSTANCE
      case _ => contextType
    }
  }

  def getContextObject(context: CommandExecutionContext): Option[BaseEntity] = {
    context.contextType match {
      case USER_HOME => None
      case SITE => getSiteByName(context.contextName)
      case INSTANCE => getInstanceByName(context.contextName)
    }
  }

  def getSiteByName(siteName: String): Option[Site1] = {
    val siteNodes = Neo4jRepository.getNodesByLabel(Site1.label)
    val sites = siteNodes.flatMap(node => Site1.fromNeo4jGraph(node.getId))
    sites.find(site => site.siteName.equals(siteName))
  }

  def getInstanceByName(instanceName: String): Option[Instance] = {
    val instnaceNodes = Neo4jRepository.getNodesByLabel(Instance.label)
    val instances = instnaceNodes.flatMap(node => Instance.fromNeo4jGraph(node.getId))
    instances.find(instance => instance.name.equals(instanceName))
  }
}
