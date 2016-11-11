/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import akka.http.scaladsl.common.StrictForm.FileData


/**
  * Created by nagulmeeras on 18/10/16.
  */
case class PlugIn(override val id: Option[Long],
                  name: String,
                  active: Boolean,
                  file: FileData) extends BaseEntity

