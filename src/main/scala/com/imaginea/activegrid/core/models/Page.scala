/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 26/9/16.
  */

case class Page[T](startIndex: Int, count: Int, totalObjects: Int, objects: List[T])

object Page {
  def apply[T](objects: List[T]): Page[T] = Page[T](0, objects.size, objects.size, objects)
}


