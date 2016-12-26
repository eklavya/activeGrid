package com.imaginea.activegrid.core.models


/**
  * Created by sivag on 7/12/16.
  */
/** TODO  application list referes to all running applications in aws account,
  * Realized when APMManager implemented
  * fetchApplicationMetrics() fucntion. */

object AutoScalingPolicyEvaluator {

  /**
    * Execises given policy criteria against running state of application/instances
    * under given AWS account and applies policy accordingly
    *
    * @param policyJob
    */
  def evaluate(policyJob: PolicyJob): Unit =
  this.synchronized {
    val scalingPolicy = policyJob.autoScalingPolicy
    val baseUri = policyJob.baseUri.getOrElse("")
    scalingPolicy.foreach { policy => policy.application.foreach { app =>
      // This application-list will be retreived throgh APMAdminManager using baseUri value.
      // Realized when APMAdminManager fully implemented
     // Please refer https://github.com/eklavya/activeGrid/issues/72
      app.apmServer.foreach { apmSrvr =>
        val apps = AdminManagerImpl.fetchApplicationMetrics(baseUri,apmSrvr)
        apps.filter(p => p.name.equals(app.name)).foreach { application =>
          if (evaluatePrimaryConditions(application, policy.primaryConditions)) {
            policy.secondaryConditions.foreach { policyCondition =>
              if (evaluateSecondaryConditions(policyCondition, policyJob.siteId, baseUri)) {
                //scalingUnit indicate increment/decremental operation in scaling.
                val scalingUnit = policyCondition.scaleType match {
                  case Some(stype) => stype match {
                    case SCALEDOWN => -1
                    case SCALEUP => 1
                    case _ => 0
                  }
                  case _ => 0
                }
                val scalingGroupId = policyCondition.scalingGroup match {
                  case Some(sgroup) => sgroup.id.getOrElse(0L)
                  case _ => 0L
                }
                //scalastyle:on magic.number
                triggerAutoScaling(policyJob.siteId, scalingGroupId, scalingUnit)
              }
            }
          }
        }
      }
    }
    }
  }

  def evaluatePrimaryConditions(application: Application, conditions: List[PolicyCondition]): Boolean = {
    conditions.exists { condition =>
      val dmyMetricType = MetricType.toMetricType("")
      val dmyConditionType = ConditionType.toconditionType("")
      val responseTimeChk = condition.metricType.getOrElse(dmyMetricType).equals(RESPONSETIME)
      val conditionChk = condition.conditionType.getOrElse(dmyConditionType).equals(GREATERTHAN)
      responseTimeChk && conditionChk
    }
  }

  def evaluateSecondaryConditions(condition: PolicyCondition, siteId: Long, baseUri: String): Boolean = {
    condition.conditionType match {
      case Some(ctype) if (ctype.conditionType.equals(CPUUTILIZATION)) =>
        condition.appTier.exists {
          appTier => appTier.instances.exists { instance =>
            val metrics = AdminManagerImpl.fetchMetricData(baseUri,siteId,instance.id.getOrElse(0L).toString,"cpu")
            metrics.map {
              mtrics => mtrics.dataPoints.exists(dp=>dp.value > condition.thresHold)
            }.getOrElse(false)
          }
        }
      case _ => false
    }
  }

  def triggerAutoScaling(siteId: Long, scalingGroupId: Long, scaleSize: Int): Unit = {
    SiteManagerImpl.setAutoScalingGroupSize(siteId, scalingGroupId, scaleSize)
  }
}
