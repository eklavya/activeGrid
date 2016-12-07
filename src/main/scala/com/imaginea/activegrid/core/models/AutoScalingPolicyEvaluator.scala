package com.imaginea.activegrid.core.models

import org.neo4j.cypher.internal.compiler.v2_2.ast.False


/**
  * Created by sivag on 7/12/16.
  */
object AutoScalingPolicyEvaluator {

  def evaluate(policyJob: PolicyJob): Unit = {
    val scalingPolicy = policyJob.autoScalingPolicy
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
            }
        }
    }
  }
  def evaluatePrimaryConditions(application: Application,conditions:List[PolicyCondition]) : Boolean = {
    conditions.forall {
      condition =>
        val responseTimeChk = condition.metricType.forall(ctype => (ctype.equals(RESPONSE)))
        val conditionChk = condition.conditionType.forall(cftype => cftype.equals(GREATERTHAN))
        responseTimeChk && conditionChk
    }
  }
  def evaluateSecondaryConditions(condition: PolicyCondition,siteId:Long,baseUri:String) : Boolean = {

    condition.conditionType match {
      case Some(ctype) if(ctype.conditionType.equals(CPUUTILIZATION)) =>
        condition.appTier.map{  appTier => appTier.instances.map{
            instance =>
              // todo instance usuage and resouce utilization will realized when APManager's 'fetchMetricData' completed.
              val metrics = new ResouceUtilization("",List.empty[DataPoint])
              metrics.getDataPoints().map{  dp => if(dp.getValue > condition.thresHold) { true }
              }
          }
        }
        true
      case _ => false
    }
  }

}
