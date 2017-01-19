package com.imaginea.activegrid.core.models

import java.io.File

import com.imaginea.activegrid.core.utils.FileUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


/**
  * Created by sivag on 19/1/17.
  */
class AnsibleScriptEngine(val inventory: Inventory, val workflowContext: WorkflowContext, val playBook: AnsiblePlayBook) {

  val workflowId: Long = workflowContext.workflow.id.getOrElse(0L)
  val logger: Logger = Logger(LoggerFactory.getLogger("AnsibleScriptEngine"))

  def executeScript() = {
    //to variable
    val variable = inventory.getExtraVariableByName("workflow-id").getOrElse(
      new Variable(None, "workflow-id", workflowId.toString, VariableScope.toVariableScope("EXTRA"), true, true))
    val invFilePath = WorkflowConstants.tmpDirLoc.concat(File.separator).concat(workflowId.toString)
    val invFile = new File(invFilePath)
    val json: String = "" //todo implementation to convert file content to json object
    FileUtils.saveContentToFile(invFile, json)
    if (invFile.exists()) {
      invFile.setExecutable(true)
    }
    val processArgs = List("/usr/bin/ansible-playbook", "-i", invFilePath, "-e", "workflowId=" + workflowId, playBook.path)
    logger.info("Triggering ansible run for playbook [" + playBook.name + "] " +
      "using inventory: " + json + " and extra variables :" + inventory.extraVariables.toString())
    logger.info("Proces")

  }

}
