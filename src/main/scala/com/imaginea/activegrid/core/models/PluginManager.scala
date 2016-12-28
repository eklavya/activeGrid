package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 22/12/16.
  */
object PluginManager {
  // Sample singnature declared
  /**
    *
    * @param pluginName
    * @return
    * Fetches and return the plugin with given name
    *
    */
    def getPluginByName(pluginName:String) : Option[PlugIn] = {
      val plugInNodes = Neo4jRepository.getSingleNodeByLabelAndProperty(PlugIn.labelName,"name",pluginName)
      plugInNodes.flatMap(plugIn => PlugIn.fromNeo4jGraph(plugIn.getId))
    }

}

