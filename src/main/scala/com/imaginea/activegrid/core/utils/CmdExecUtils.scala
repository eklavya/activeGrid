package com.imaginea.activegrid.core.utils

import com.imaginea.activegrid.core.models._

/**
  * Created by nagulmeeras on 07/12/16.
  */
object CmdExecUtils {
  def getDervivedContext(context: String, cmdExecutionContext: CommandExecutionContext): Option[CommandExecutionContext] = {
    val targetContext = context.trim.replaceAll("\\\\", "/")
    val paths = targetContext.split("/")

    val execContext = targetContext.startsWith("/") match {
      case true => getExecContext(cmdExecutionContext)
      case false => Some(cmdExecutionContext)
    }
    paths.foldLeft(execContext) { (execContext, contextPath) =>
      if (contextPath.isEmpty || contextPath.equals("/") || contextPath.equals(".")) {
        execContext
      } else if (contextPath.equals("..")) {
        val parentContext = execContext.flatMap(_.parentContext)
        if (parentContext.isEmpty) throw new Exception("No such context to navigate")
        parentContext
      } else {
        execContext.map { context =>
          val contextType = getContextType(context)
          val contextObject = getContextObject(context)
          CommandExecutionContext.apply(contextPath, contextType, contextObject, execContext)
        }
      }
    }
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

  def getExecContext(cmdExecutionContext: CommandExecutionContext): Option[CommandExecutionContext] = {
    if (cmdExecutionContext.contextType.contextType.equals(USER_HOME.contextType)) {
      Some(cmdExecutionContext)
    } else {
      cmdExecutionContext.parentContext.flatMap(ctx => getExecContext(ctx))
    }
  }
}
