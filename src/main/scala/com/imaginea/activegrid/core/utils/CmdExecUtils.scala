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
      case true =>
        if (cmdExecutionContext.contextType.contextType.equals(USER_HOME.contextType)) {
          Some(cmdExecutionContext)
        } else {
          val context = cmdExecutionContext.parentContext
          context.foldLeft(context) { (currentCtx, initCtx) =>
            currentCtx.flatMap { currCtx =>
              if (currCtx.contextType.contextType.equals(USER_HOME.contextType)) {
                currentCtx
              } else {
                currCtx.parentContext
              }
            }
          }
        }

      case false => Some(cmdExecutionContext)
    }
    paths.foldLeft(execContext) { (execContext, contextPath) =>
      contextPath match {
        case path if path.isEmpty || path.equals("/") || path.equals(".") => execContext
        case path if path.equals("..") =>
          val parentContext = execContext.flatMap(_.parentContext)
          if (parentContext.isEmpty) throw new Exception("No such context to navigate")
          parentContext
        case _ =>
          execContext.map { context =>
            val contextType = getContextType(context)
            val contextObject = getContextObject(context)
            CommandExecutionContext.apply(contextPath, contextType, contextObject, execContext)
          }
      }
    }
    execContext
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
