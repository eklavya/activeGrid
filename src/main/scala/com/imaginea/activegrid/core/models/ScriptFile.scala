package com.imaginea.activegrid.core.models

import java.io.File

/**
  * Created by shareefn on 20/12/16.
  */
case class ScriptFile(override val id: Option[Long],
                      name: String,
                      path: String,
                      file: Option[File]) extends BaseEntity