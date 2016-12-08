package com.imaginea.activegrid.core.models



/**
  * Created by sivag on 7/12/16.
  */
object AutoScalingPolicyEvaluator {

  def evaluate(policyJob: PolicyJob): Unit = {
    val scalingPolicy = policyJob.autoScalingPolicy
    val baseUri = policyJob.baseUri match {case Some(uri) => uri case _ => ""}
    scalingPolicy.map
    {
      policy =>
        policy.application.map{
          app =>
           // TODO below application list referes to all running applications in aws account,
            // Realized when APMManager implemented
            // fetchApplicationMetrics() fucntion. */
            val apps :List[Application] = List.empty[Application]
            apps.filter(p => p.name.equals(app.name)).map{
              application =>
                if(evaluatePrimaryConditions(application,policy.primaryConditions))
                {
                  policy.secondaryConditions.foreach
                  {
                    policyCondition =>
                      if(evaluateSecondaryConditions(policyCondition,policyJob.siteId,baseUri)){
                         val scaleType = policyCondition.scaleType match { case Some(stype) => stype case _ => ScaleType.toScaleType("")}
                         val scaleSize = scaleType.equals(SCALEUP)

                      }
                  }
                }
                policy

            }
        }
    }
  }
  def evaluatePrimaryConditions(application: Application,conditions:List[PolicyCondition]) : Boolean = {

    conditions.exists{
      condition =>
        val responseTimeChk = condition.metricType.forall(ctype => (ctype.equals(RESPONSE)))
        val conditionChk = condition.conditionType.forall(cftype => cftype.equals(GREATERTHAN))
        responseTimeChk && conditionChk
    }
  }
  def evaluateSecondaryConditions(condition: PolicyCondition,siteId:Long,baseUri:String) : Boolean = {

    condition.conditionType match {
      case Some(ctype) if(ctype.conditionType.equals(CPUUTILIZATION)) =>
        condition.appTier.forall{
          appTier => appTier.instances.exists{ instance =>
              // todo instance usuage and resouce utilization will realized when APManager's 'fetchMetricData' completed.
              val metrics = new ResouceUtilization("",List.empty[DataPoint])
              metrics.getDataPoints().indexWhere( dp => dp.getValue > condition.thresHold) > -1
          }
        }
      case _ => false
    }
  }
  def triggerAutoScaling(siteId:Long,scalingGroupId:Long,scaleSize:Int)  = {
    SiteManagerImpl.setAutoScalingGroupSize(siteId,scalingGroupId,scaleSize)
  }
}
