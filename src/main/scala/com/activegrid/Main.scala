package com.activegrid

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.model._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}
import scala.util.{Success,Failure}
import scala.concurrent.Future

object Main {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  /*
  Scala futures need an execution context to run on.
  It works exactly like a runnable queue where futures are enqueued and the execution threads (in the thread pool)
  dequeue them and execute.

  Implicit values are hidden arguments.
  Let's say you have a common parameter that needs to be an argument to varioud functions.
  It could be part of config or infrastructure. Instead of passing it to each and every function.
  We can make it implicit and all functions which expect an implicit argument will get it from the local scope.

  read more about futures and implicits.
   */
  implicit val executionContext = system.dispatcher

  // domain model
  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  // formats for unmarshalling and marshalling

  /*
  Here we are creating json codec automatically.
   */
  implicit val itemFormat = jsonFormat2(Item)

  implicit val orderFormat = jsonFormat1(Order)

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = Future(Some(Item("item", itemId)))

  def saveOrder(order: Order): Future[Done] = Future(Done)

  def main(args: Array[String]) {

    val itemRoute = get {
      pathPrefix("item" / LongNumber) { id =>

        val maybeItem: Future[Option[Item]] = fetchItem(id)

        onSuccess(maybeItem) {
          case Some(item) => complete(item)
          case None => complete(StatusCodes.NotFound)
        }
      }
    }

    val orderRoute = post {
      path("create-order") {
        entity(as[Order]) { order =>
          val saved: Future[Done] = saveOrder(order)
          onComplete(saved) { done =>
            complete("order created")
          }
        }
      }
    }

    implicit val ImageFormat = jsonFormat(ImageInfo.apply, "id", "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
    implicit val PageImgFormat = jsonFormat4(Page[ImageInfo])
    implicit val InstanceFlavorFormat = jsonFormat4(InstanceFlavor.apply)
    implicit val PageInstFormat = jsonFormat4(Page[InstanceFlavor])
    implicit val storageInfoFormat = jsonFormat3(StorageInfo.apply)
    implicit val KeyValueInfoFormat = jsonFormat3(KeyValueInfo.apply)
    implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {
      override def write(obj: KeyPairStatus): JsValue = JsString(obj.value.toString)

      override def read(json: JsValue): KeyPairStatus = json match {
        case JsString(str) => str
        case _ => throw DeserializationException("Enum string expected")
      }
    }
    implicit val keyPairInfoFormat = jsonFormat8(KeyPairInfo.apply)
    implicit val sshAccessInfoFormat = jsonFormat4(SSHAccessInfo.apply)
    implicit val portRangeFormat = jsonFormat3(PortRange.apply)
    implicit val instanceConnectionFormat = jsonFormat4(InstanceConnection.apply)
    implicit val softwareForamt = jsonFormat8(Software.apply)
    implicit val processInfoFormat = jsonFormat9(ProcessInfo.apply)
    implicit val instanceUserFormat = jsonFormat3(InstanceUser.apply)
    implicit val instanceFormat = jsonFormat18(Instance.apply)
    implicit val PageInstanceFormat = jsonFormat4(Page[Instance])
    implicit val SiteFormat = jsonFormat2(Site.apply)

    val catalogRoute = pathPrefix("catalog") {

      path("images" / "view") {
        get {
          val listOfImages: Future[Page[ImageInfo]] = Future {
            val label: String = "ImageInfo"
            val nodesList = GraphDBExecutor.getNodesByLabel(label)
            val imageInfoList = nodesList.flatMap(node => ImageInfo.fromNeo4jGraph(node.getId))

            Page[ImageInfo](imageInfoList)
          }
          onComplete(listOfImages) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to Retrieve ImageInfo List; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to Retrieve ImageInfo List")
          }
        }
      } ~ path("images") {
        put {
          entity(as[ImageInfo]) { image =>
            val buildImage = Future {
              image.toNeo4jGraph(image)
              "Successfully added ImageInfo"
            }
            onComplete(buildImage) {
              case Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case Failure(ex) =>
                logger.error(s"Unable to Save Image; Failed with ${ex.getMessage}", ex)
                complete(StatusCodes.BadRequest, "Unable to Save Image")
            }
          }
        }
      } ~ path("images" / LongNumber) { imageId =>
        delete {
          val deleteImages = Future {
            GraphDBExecutor.deleteEntity[ImageInfo](imageId)
            "Successfully deleted ImageInfo"
          }
          onComplete(deleteImages) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to Delete Image; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to Delete Image")
          }

        }
      } ~ path("instanceTypes" / IntNumber) { siteId =>
        get {
          val listOfInstanceFlavors = Future {
            val mayBeSite = Site.fromNeo4jGraph(siteId)
            mayBeSite match {
              case Some(site) =>
                val listOfInstances = site.instances
                val listOfInstanceFlavors = listOfInstances.map(instance => InstanceFlavor(instance.instanceType.get, None, instance.memoryInfo.get.total, instance.rootDiskInfo.get.total))
                Page[InstanceFlavor](listOfInstanceFlavors)

              case None =>
                logger.warn(s"Failed while doing fromNeo4jGraph of Site for siteId : $siteId")
                Page[InstanceFlavor](List.empty[InstanceFlavor])
            }
          }
          onComplete(listOfInstanceFlavors) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get List; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, "Unable to get List of Instance Flavors")
          }
        }
      }
    }

    def nodeRoute = pathPrefix("node") {
      path("list") {
        get {
          val listOfAllInstanceNodes = Future {
            logger.info("Received GET request for all nodes")
            val label: String = "Instance"
            val nodesList = GraphDBExecutor.getNodesByLabel(label)
            val instanceList = nodesList.flatMap(node => Instance.fromNeo4jGraph(node.getId))
            Page[Instance](instanceList)
          }
          onComplete(listOfAllInstanceNodes) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get Instance nodes; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Unable to get Instance nodes")
          }
        }
      } ~ path("topology") {
        get {
          val topology = Future {
            logger.debug("received GET request for topology")
            Page[Instance](List.empty[Instance])
          }
          onComplete(topology) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get Instance nodes; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Unable to get Instance nodes")
          }
        }
      } ~ path(Segment) { name =>
        get {
          val nodeInstance = Future {
            logger.info(s"Received GET request for node - $name")
            if (name == "localhost") {
              val instance = Instance(name)
              instance.toNeo4jGraph(instance)
            }
            val instanceNode = GraphDBExecutor.getNodeByProperty("Instance", "name", name)
            instanceNode match {
              case Some(node) => Instance.fromNeo4jGraph(node.getId).get
              case None =>
                val name = "echo node"
                val tags: List[KeyValueInfo] = List(KeyValueInfo(None, "tag", "tag"))
                val processInfo = ProcessInfo(1, 1, "init")
                Instance(name, tags, Set(processInfo))
            }
          }
          onComplete(nodeInstance) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(ex) =>
              logger.error(s"Unable to get Instance with name $name; Failed with ${ex.getMessage}", ex)
              complete(StatusCodes.BadRequest, s"Unable to get Instance with name $name")
          }
        }
      }
    }

    val route = itemRoute ~ orderRoute ~ catalogRoute ~ nodeRoute

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}
