package com.imaginea.activegrid.core.models

import java.io.{File, FileInputStream}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.yaml.snakeyaml.{DumperOptions, Yaml}
import scala.collection.JavaConversions._ //scalastyle:ignore underscore.import
import scala.io.Source.fromFile

/**
  * Created by shareefn on 17/3/17.
  */

class AnsiblePlayBookParser {

  val yaml = getYaml
  val definedVariables = scala.collection.mutable.Map.empty[String, Any]
  val undefinedVariableNames = scala.collection.mutable.Set.empty[String]
  val taskList = scala.collection.mutable.ListBuffer[Task]()

  def buildPlayBook(playBook: AnsiblePlayBook, playBookBaseDir: String): AnsiblePlayBook = {
    val playBookName = playBook.name match {
      case Some(name) => name
      case None => throw new Exception("play book name is empty")
    }
    val plays = parseYaml(playBookName, playBookBaseDir)
    filterVariables()
    val variableList = getAnsibleVariableList(undefinedVariableNames.toSet, VariableScope.toVariableScope("UNKNOWN"))
    playBook.copy(playList = plays, variables = variableList)
  }

  def parseYaml(playBookName: String, baseDir: String, plays: List[AnsiblePlay] = List.empty[AnsiblePlay]): List[AnsiblePlay] = {
    val playList = getYamlDataAsList(baseDir + java.io.File.separator + playBookName)
    if(playList.isEmpty) throw new Exception("no plays found in the playbook")
    playList.foldLeft(plays) { (list,playMap) =>
      if(playMap.size == 1 && playMap.isDefinedAt(AnsibleConstants.AnsiblePlayInclude)) {
        val tokens = playMap.get(AnsibleConstants.AnsiblePlayInclude).asInstanceOf[String].split("\\s+")
        val includedYaml = tokens(0)
        parseYaml(includedYaml, baseDir, list)
      } else if(playMap.isDefinedAt(AnsibleConstants.AnsiblePlayHosts)) {
        val play = getPlay(playMap, baseDir)
        play.copy(path = Some(baseDir.concat(File.separator).concat(playBookName)))
        play :: list
      } else {
        plays
      }
    }
  }

  def getPlay(playMap: Map[String, Any], baseDir: String): AnsiblePlay = {
    val playName = playMap.keySet.foldLeft(None: Option[String]) { (name, key) =>
      if(AnsibleConstants.AnsiblePlayHosts.equals(key)) {
        val nodes = playMap.get(key).asInstanceOf[Option[String]]
        if(name.isEmpty) nodes else name
      } else if(AnsibleConstants.AnsibleName.equals(key)) {
        playMap.get(key).asInstanceOf[Option[String]]
      } else if(AnsibleConstants.AnsiblePlayRoles.equals(key)) {
        val roleBaseDir = baseDir + File.separator + key
        parseRoles(playMap(key).asInstanceOf[List[Any]], roleBaseDir)
        name
      } else if(AnsibleConstants.AnsiblePlayTasks.equals(key) || AnsibleConstants.AnsiblePlayHandlers.equals(key)) {
        parseTasks(playMap.getOrElse(key, List.empty[Map[String, Any]]).asInstanceOf[List[Map[String, Any]]], baseDir, key)
        name
      } else if(AnsibleConstants.AnsibleVars.equals(key)) {
        val inlineVars = playMap(key).asInstanceOf[Map[String, Any]]
        definedVariables ++ inlineVars
        name
      } else { name }
    }
    val groupVarYaml = baseDir + File.separator + AnsibleConstants.AnsibleGroupVarsDir + File.separator
    val groupVariables = parseVariables(groupVarYaml)
    val group = if(groupVariables.nonEmpty) {
      definedVariables ++ groupVariables
      Some(AnsibleGroup(None, None, getAnsibleVariableList(groupVariables, VariableScope.toVariableScope("GROUP"))))
    } else { None }
    val content = yaml.dump(playMap)
    AnsiblePlay(playName, taskList.toList, content, group)
  }

  def parseTasks(tasks: List[Map[String, Any]], taskBaseDir: String, taskType: String): Unit = {
    tasks.foreach { task =>
      val content = yaml.dump(task)
      val path = FilenameUtils.getFullPathNoEndSeparator(taskBaseDir)
      val taskName = if (task.isDefinedAt(AnsibleConstants.AnsibleName)) {
        task(AnsibleConstants.AnsibleName).asInstanceOf[String]
      } else {
        val (key, value) = task.head
        key + " " + value
      }
      val newTaskType = if (AnsibleConstants.AnsiblePlayTasks.equals(taskType)) {
        TaskType.toTaskType("TASK")
      } else {
        TaskType.toTaskType("HANDLER_TASK")
      }
      val taskObj = Task(None, taskName, content, newTaskType, path)
      taskList += taskObj
      // process each task entry for variables
      task.keySet.foreach { e =>
        undefinedVariableNames ++ getVarsFromString(task(e))
        if (AnsibleConstants.AnsiblePlayInclude.equals(e)) {
          // handle if any vars are passed to include yaml
          val tokens = task(e).asInstanceOf[String].split("\\s+")
          val taskYaml = taskBaseDir + File.separator + tokens(0)
          val includedTasks = getYamlDataAsList(taskYaml)
          parseTasks(includedTasks, FilenameUtils.getFullPath(taskYaml), taskType)
        } else if (AnsibleConstants.AnsiblePlayLoop.equals(e)) {
          val value = task(e)
          if (value.isInstanceOf[String]) {
            val valueStr = value.asInstanceOf[String]
            if (valueStr.contains("{{")) {
              undefinedVariableNames ++ getVarsFromString(valueStr)
            } else {
              val mayBeVariable = getVar(valueStr)
              mayBeVariable.map(undefinedVariableNames.add)
            }
          } else if (AnsibleConstants.AnsibleRegisterVariable.equals(e)) {
            definedVariables.put(task(e).asInstanceOf[String], AnsibleConstants.AnsibleRegisterVariable)
          }
        }
      }
    }
  }
  // scalastyle:off cyclomatic.complexity
  def parseRoles(roles: List[Any], roleBaseDir: String): Unit = {
    roles.foreach { role =>
      val roleName = role match {
        case s: String => s
        case m: Map[String, String] => m(AnsibleConstants.AnsiblePlayRole)
        case _ => ""
      }
      val roleDir = roleBaseDir + File.separator + roleName
      val f = new File(roleDir)
      if(f.isDirectory) {
        val fl = f.listFiles()
        fl.foreach { fc =>
          if(fc.isDirectory) {
            // process dependent roles
            if (AnsibleConstants.AnsiblePlayRoleMeta.equals(fc.getName)) {
              val metaYaml = getRolePath(roleDir,AnsibleConstants.AnsiblePlayRoleMeta)
              val dependencies = getYamlDataAsMap(metaYaml)
              if (dependencies.nonEmpty && dependencies.isDefinedAt(AnsibleConstants.AnsiblePlayRoleDependencies)) {
                parseRoles(dependencies(AnsibleConstants.AnsiblePlayRoleDependencies).asInstanceOf[List[Any]],roleBaseDir)
              }
              // process role tasks
            } else if (AnsibleConstants.AnsiblePlayTasks.equals(fc.getName)
              || AnsibleConstants.AnsiblePlayHandlers.equals(fc.getName)) {
              val taskYaml = getRolePath(roleDir,fc.getName)
              val tasks = getYamlDataAsList(taskYaml)
              parseTasks(tasks,FilenameUtils.getFullPath(taskYaml), fc.getName)
              // process role variables
            } else if (AnsibleConstants.AnsibleVars.equals(fc.getName)) {
              val varYaml = getRolePath(roleDir,AnsibleConstants.AnsibleVars)
              definedVariables ++ parseVariables(varYaml)
              // process role default vars
            } else if (AnsibleConstants.AnsibleRoleDefaultVars.equals(fc.getName)) {
              val varYaml = getRolePath(roleDir, AnsibleConstants.AnsibleRoleDefaultVars)
              definedVariables ++ parseVariables(varYaml)
              // process role templates
            } else if (AnsibleConstants.AnsibleTaskTemplates.equals(fc.getName)) {
              val filters = Array( "j2" )
              val templateFiles = FileUtils.listFiles(fc, filters, true).asInstanceOf[List[File]]
              templateFiles.foreach { tf =>
                undefinedVariableNames ++ getVarsFromTemplate(tf)
              }
            }
          }
        }
      }
    }
  }

  def getYamlDataAsList(yamlFile: String): List[Map[String, Any]] = {
    val f = new File(yamlFile)
    if (f.exists()) {
      val yamlData = yaml.load(new FileInputStream(f)).asInstanceOf[java.util.List[java.util.Map[String,Any]]]
      yamlData.toList.map(_.toMap)
    } else { List.empty[Map[String, Any]] }
  }

  def getYamlDataAsMap(yamlFile: String): Map[String, Any] = {
    val f = new File(yamlFile)
    if (f.exists()) {
      yaml.load(new FileInputStream(f)).asInstanceOf[java.util.Map[String,Any]].toMap
    } else { Map.empty[String, Any] }
  }

  def getVarsFromString(s: Any): List[String] = {
    if(!s.isInstanceOf[String]) { List.empty[String] }
    else {
      val tokens = s.asInstanceOf[String].split("\\{{2}")
      val varList = tokens.foldLeft(List.empty[String]) { (list, token) =>
        if(token.contains("}}")) {
          val subTokens = token.split("\\}{2}")
          val mayBeAnsibleVariable = getVar(subTokens(0).trim)
          mayBeAnsibleVariable match {
            case Some(variable) => variable :: list
            case None => list
          }
        } else { list }
      }
      varList
    }
  }

  def getVar(variable: String): Option[String] = {
    val ansibleVariable = if (variable.contains(".")) {
      variable.split("\\.")(0)
    } else if (variable.contains("|")) {
      variable.split("\\|")(0)
    } else { variable }

    if (!ansibleVariable.contains(AnsibleConstants.AnsibleLoopVariable)
      && !ansibleVariable.startsWith(AnsibleConstants.AnsibleSelf)
      && !ansibleVariable.startsWith(AnsibleConstants.AnsibleHostVars)
      && !AnsibleConstants.AnsibleInventoryHostname.equals(ansibleVariable)
      && !ansibleVariable.contains(AnsibleConstants.AnsibleInventoryGroups)
      && !ansibleVariable.contains(AnsibleConstants.AnsibleLookup)) {
      Some(ansibleVariable)
    } else { None }
  }

  def getHostVar(line: String): Option[String] = {
    val tokens = line.split(AnsibleConstants.AnsibleHostVars).flatMap(_.split("\\[|\\]|\\."))
    val hostVar = tokens.find(_.matches("^'.+'$"))
    hostVar.map(token => token.substring(1, token.length - 1))
  }

  def getVarsFromTemplate(f: File): List[String] = {
    if(!f.exists) { List.empty[String] }
    else {
      val lines = fromFile(f.getAbsolutePath).getLines
      val varList = lines.foldLeft(List.empty[String]) { (list,line) =>
        if (line.contains(AnsibleConstants.AnsibleHostVars)) {
          val mayBeHostVar = getHostVar(line)
          mayBeHostVar match {
            case Some(v) => v:: list
            case None => list
          }
        } else { list }
      }
      varList
    }
  }

  def getAnsibleVariableList(varMap: Map[String, Any], scope: VariableScope): List[Variable] = {
    varMap.foldLeft(List.empty[Variable]) { (list, entry)=>
      val (name, variableValue) = entry
      Variable(None, name, Some(variableValue.toString), scope, readOnly = false, hidden = false) :: list
    }
  }

  def getAnsibleVariableList(varList: Set[String], scope: VariableScope): List[Variable] = {
    varList.foldLeft(List.empty[Variable]) { (list,name) =>
      Variable(None, name, None, scope, readOnly = false, hidden = false) :: list
    }
  }

  def parseVariables(varFile: String): Map[String, Any] = {
    getYamlDataAsMap(varFile)
  }

  def getRolePath(baseDir: String, varType: String): String = {
    baseDir + File.separator + varType + File.separator + AnsibleConstants.AnsibleMainYaml
  }

  def filterVariables(): Unit = {
    definedVariables.keySet.foreach { key =>
      if(undefinedVariableNames.contains(key)) {
        undefinedVariableNames.remove(key)
      }
    }
  }

  def getYaml: Yaml = {
    val options = new DumperOptions()
    val indentation = 4
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    options.setExplicitStart(true)
    options.setIndent(indentation)
    val yaml = new Yaml(options)
    yaml
  }
}
