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
  def getPlugin(pluginName: String): Option[PlugIn] = {
    //todo implementation pending
    // Dummy response
    PlugIn.fromNeo4jGraph(0L)
  }

}

