package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 25/10/16.
  */
case class SiteFilter(override val id: Option[Long], accountInfo: AccountInfo, filters: List[Filter]) extends BaseEntity
