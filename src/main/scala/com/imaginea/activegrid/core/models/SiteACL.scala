package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.discovery.models.{Instance, Site}

/**
 * Created by ranjithrajd on 10/10/16.
 */
class SiteACL(override val id: Option[Long]
              , name: String
              , site: Site
              , instances: List[Instance]
              , groups: List[UserGroup]) extends BaseEntity {
}
