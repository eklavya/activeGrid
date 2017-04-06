package com.imaginea.activegrid.core.models

import java.io.File

import com.imaginea.activegrid.core.utils.FileUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.sys.process._
import scala.util.Try

/**
  * Created by sivag on 23/3/17.
  */
class AnsibleScriptEngine(val inventory: Inventory, val workflowContext: WorkflowContext, val playBook: AnsiblePlayBook) {
  val logger: Logger = Logger(LoggerFactory.getLogger("AnsibleScriptEngine"))

  /**
    * Creates process to run playbook and starts the process.
    * Writes output to a logfile.
    *
    * @return true if process started successfully.
    */
  def executeScript(): Try[Boolean] = {
    Try {
      if (!workflowContext.workflow.id.isDefined) {
        logger.error("Invalid workflow details provided")
        false
      } else {
        //scala-style:off
        val workflowId: Long = workflowContext.workflow.id.get
        val logFile = new File(WorkflowConstants.homeDir.concat(s"/logs/${File.separator}").concat(s"${workflowId.toString}.log"))
        val variable = inventory.getExtraVariableByName("workflow-id").getOrElse(
          new Variable(None, "workflow-id", Some(workflowId.toString), VariableScope.toVariableScope("EXTRA"), true, true))
        val invFilePath = WorkflowConstants.tmpDirLoc.concat(File.separator).concat(workflowId.toString)
        val invFile = new File(invFilePath)
        val json: String = "" //todo implementation to convert file content to json object
        FileUtils.saveContentToFile(invFile, json)
        if (invFile.exists()) {
          invFile.setExecutable(true)
        }
        val command = Seq("/usr/bin/ansible-playbook", "-i", invFilePath.toString, "-e", "workflowId=" + workflowId.toString, playBook.path.toString)
        logger.info(s"Starting playbook ${playBook.name} using inventory $json and extra variables : ${inventory.extraVariables.toString()}")
        val lines = Process(command) lineStream_! (ProcessLogger(logFile))
        lines.foreach(line => FileUtils.saveContentToFile(logFile, line))
        logger.info(s"Workflow ${workflowId.toString} has started, Please refer logfile ${logFile}")
        true
      }
    }
  }
}
