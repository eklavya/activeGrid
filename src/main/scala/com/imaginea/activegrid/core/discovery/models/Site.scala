package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.policy.models.AutoScalingPolicy

/**
 * Created by ranjithrajd on 10/10/16.
 */
class Site(siteName: String,
           instances: List[Instance] = List.empty,
           filters: List[Filter] = List.empty,
           keypairs: List[InstanceGroup] = List.empty,
           applications: List[Application] = List.empty,
           groupBy: String,
           loadBalancers: List[LoadBalancer] = List.empty,
           scalingGroups: List[ScalingGroup] = List.empty,
           reservedInstanceDetails: List[ReservedInstanceDetails] = List.empty,
           scalingPolicies: List[AutoScalingPolicy] = List.empty
            ) {

}
