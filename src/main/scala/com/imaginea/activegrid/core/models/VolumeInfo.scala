package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class VolumeInfo(override val id: Option[Long],
                      volumeId: String,
                      size: Int,
                      snapshotId: String,
                      availabilityZone: String,
                      state: String,
                      createTime: String,
                      tags: List[KeyValueInfo],
                      volumeType: String,
                      snapshotCount: Int,
                      currentSnapshot: Option[SnapshotInfo]) extends BaseEntity
