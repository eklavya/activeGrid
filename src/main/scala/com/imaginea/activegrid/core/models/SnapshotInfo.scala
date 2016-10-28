package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class SnapshotInfo(override val id: Option[Long],
                        snapshotId: String,
                        volumeId: String,
                        state: String,
                        startTime: String,
                        progress: String,
                        ownerId: String,
                        ownerAlias: String,
                        description: String,
                        volumeSize: Int,
                        tags: List[KeyValueInfo]) extends BaseEntity
