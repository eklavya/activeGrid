package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */
sealed trait AWSInstanceType {
  def instanceType: String

  def ramSize: Double

  def rootPartitionSize: Double
}

case object AWSInstanceType {

  case object T1Micro extends AWSInstanceType {
    override def instanceType: String = "t1.micro"

    override def ramSize: Double = 0.613D

    override def rootPartitionSize: Double = 8D
  }

  case object T2Micro extends AWSInstanceType {
    override def instanceType: String = "t2.micro"

    override def ramSize: Double = 0.613D

    override def rootPartitionSize: Double = 8D
  }

  case object M1Small extends AWSInstanceType {
    override def instanceType: String = "m1.small"

    override def ramSize: Double = 1.7D

    override def rootPartitionSize: Double = 8D
  }

  case object M1Medium extends AWSInstanceType {
    override def instanceType: String = "m1.medium"

    override def ramSize: Double = 3.7D

    override def rootPartitionSize: Double = 8D
  }

  case object M1Large extends AWSInstanceType {
    override def instanceType: String = "m1.large"

    override def ramSize: Double = 7.5D

    override def rootPartitionSize: Double = 10D
  }

  case object M1XLarge extends AWSInstanceType {
    override def instanceType: String = "m1.xLarge"

    override def ramSize: Double = 15D

    override def rootPartitionSize: Double = 10D
  }

  case object M3Medium extends AWSInstanceType {
    override def instanceType: String = "m3.medium"

    override def ramSize: Double = 3.75D

    override def rootPartitionSize: Double = 10D
  }

  case object M3Large extends AWSInstanceType {
    override def instanceType: String = "m3.large"

    override def ramSize: Double = 7D

    override def rootPartitionSize: Double = 10D
  }

  case object M3XLarge extends AWSInstanceType {
    override def instanceType: String = "m3.xlarge"

    override def ramSize: Double = 15D

    override def rootPartitionSize: Double = 10D
  }

  case object M32XLarge extends AWSInstanceType {
    override def instanceType: String = "m3.2xlarge"

    override def ramSize: Double = 30D

    override def rootPartitionSize: Double = 10D
  }

  case object M2XLarge extends AWSInstanceType {
    override def instanceType: String = "m2.xlarge"

    override def ramSize: Double = 34.2D

    override def rootPartitionSize: Double = 10D
  }

  case object M22XLarge extends AWSInstanceType {
    override def instanceType: String = "m2.2xlarge"

    override def ramSize: Double = 17.1D

    override def rootPartitionSize: Double = 10D
  }

  case object M24XLarge extends AWSInstanceType {
    override def instanceType: String = "m2.4xlarge"

    override def ramSize: Double = 68.4D

    override def rootPartitionSize: Double = 10D
  }

  case object HI14XLarge extends AWSInstanceType {
    override def instanceType: String = "hi1.4xlarge"

    override def ramSize: Double = 0.613D

    override def rootPartitionSize: Double = 8D
  }

  case object HI18XLarge extends AWSInstanceType {
    override def instanceType: String = "hi1.8xlarge"

    override def ramSize: Double = 117D

    override def rootPartitionSize: Double = 10D
  }

  case object C1Medium extends AWSInstanceType {
    override def instanceType: String = "c1.medium"

    override def ramSize: Double = 1.7D

    override def rootPartitionSize: Double = 10D
  }

  case object C1XLarge extends AWSInstanceType {
    override def instanceType: String = "c1.xlarge"

    override def ramSize: Double = 7D

    override def rootPartitionSize: Double = 10D
  }

  case object C3Large extends AWSInstanceType {
    override def instanceType: String = "c3.large"

    override def ramSize: Double = 3.75D

    override def rootPartitionSize: Double = 10D
  }

  case object C3XLarge extends AWSInstanceType {
    override def instanceType: String = "c3.xlarge"

    override def ramSize: Double = 7.5D

    override def rootPartitionSize: Double = 10D
  }

  case object C32XLarge extends AWSInstanceType {
    override def instanceType: String = "c3.2xlarge"

    override def ramSize: Double = 15D

    override def rootPartitionSize: Double = 10D
  }

  case object C34XLarge extends AWSInstanceType {
    override def instanceType: String = "c3.4xlarge"

    override def ramSize: Double = 30D

    override def rootPartitionSize: Double = 10D
  }

  case object C38XLarge extends AWSInstanceType {
    override def instanceType: String = "c3.8xlarge"

    override def ramSize: Double = 60D

    override def rootPartitionSize: Double = 10D
  }

  case object NEWERBETTER extends AWSInstanceType {
    override def instanceType: String = "unknown"

    override def ramSize: Double = 1D

    override def rootPartitionSize: Double = 1D
  }
  //scalastyle:off
  def toAWSInstanceType(awsInstanceName: String): AWSInstanceType = {
    awsInstanceName match {
      case "t1.micro" => T1Micro
      case "t2.micro" => T2Micro
      case "m1.small" => M1Small
      case "m1.medium" => M1Medium
      case "m1.large" => M1Large
      case "m1.xlarge" => M1XLarge
      case "m3.medium" => M3Medium
      case "m3.large" => M3Large
      case "m3.xlarge" => M3XLarge
      case "m3.2xlarge" => M32XLarge
      case "m2.xlarge" => M2XLarge
      case "m2.2xlarge" => M22XLarge
      case "m2.4xlarge" => M24XLarge
      case "hi1.4xlarge" => HI14XLarge
      case "hi1.8xlarge" => HI18XLarge
      case "c1.medium" => C1Medium
      case "c1.xlarge" => C1XLarge
      case "c3.large" => C3Large
      case "c3.xlarge" => C3XLarge
      case "c3.2xlarge" => C32XLarge
      case "c3.4xlarge" => C34XLarge
      case "c3.8xlarge" => C38XLarge
      case _ => NEWERBETTER
    }
  }
  //scalastyle:on
}
