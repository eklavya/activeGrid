package com.activegrid.model

/**
  * Created by shareefn on 22/9/16.
  */
case class ImageInfo(imageId : String,
                     state : String,
                     ownerId  :String,
                     publicValue :Boolean,
                     architecture  :String,
                     imageType  :String,
                     platform  :String,
                     imageOwnerAlias  :String,
                     name  :String,
                     description  :String,
                     rootDeviceType  :String,
                     rootDeviceName  :String,
                     version  :String) extends BaseEntity



