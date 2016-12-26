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
    def getPlugin(pluginName:String) : Option[PlugIn] = {
      Neo4jRepository.getNodesByLabel(PlugIn.labelName).filter {
        plugin =>   plugin.hasProperty("name") && plugin.getProperty("name").toString.equals(pluginName)
      }.flatMap {
        plgnNode => PlugIn.fromNeo4jGraph(plgnNode.getId)
      }.headOption
    }

}

