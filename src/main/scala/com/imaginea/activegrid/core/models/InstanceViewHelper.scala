package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 08/12/16.
  */
object InstanceViewHelper {
  def fetchInstance(instance: Instance, viewLevel: ViewLevel): List[String] = {
    viewLevel.viewLevel match {
      case DETAILED.viewLevel =>
        List(Some(instance.name), instance.instanceId, instance.publicDnsName, instance.id.map(id => id.toString)).flatten
      case _ => List(instance.name)
    }
  }
}
