package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 25/10/16.
  */
case class AccountInfo( override val id: Option[Long],
                        accountId : Option[String] ,
                        providerType: InstanceProvider, // AWS or OpenStack or Physical LAN etc..
                        ownerAlias: Option[String], //optional
                        accessKey: String,
                        secretKey: String ,
                        regionName: String , // region or end-point
                        regions: List[String],
                        networkCIDR: String) extends BaseEntity
