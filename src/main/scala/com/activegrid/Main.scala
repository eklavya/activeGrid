package com.activegrid

import akka.Done
import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Date
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.activegrid.model._
import com.activegrid.services.{CatalogService, NodeService, TestService}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.concurrent.Future
import scala.util._

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
          case None       => complete(StatusCodes.NotFound)
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

    implicit val ImageFormat = jsonFormat(ImageInfo.apply, "id","imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
    implicit val PageImgFormat = jsonFormat4(Page[ImageInfo])
    implicit val InstanceFlavorFormat = jsonFormat4(InstanceFlavor.apply)
    implicit val PageInstFormat = jsonFormat4(Page[InstanceFlavor])
    implicit val storageInfoFormat = jsonFormat3(StorageInfo.apply)
    implicit val tupleFormat = jsonFormat3(Tuple.apply)
    implicit object KeyPairStatusFormat extends RootJsonFormat[KeyPairStatus] {
      override def write (obj: KeyPairStatus): JsValue = JsString(obj.value.toString)

      override def read(json: JsValue): KeyPairStatus = json match {
        case JsString(str) => str
        case _ => throw new DeserializationException("Enum string expected")
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

    var catalogService = new CatalogService()

    val catalogRoutes  = pathPrefix("catalog") {

      path("images"/"view") {
        get {
          val listOfImages : Future[Page[ImageInfo]] = catalogService.getImages()

          onComplete(listOfImages) {
             case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
             case util.Failure(ex) => complete(StatusCodes.BadRequest, "Unable to Retrieve ImageInfo List; Failed with " + ex)
           }
        }
      } ~ path("images") {
        put {
          entity(as[ImageInfo]) { image =>
            val buildImage = catalogService.buildImage(image)
            onComplete(buildImage) {
              case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case util.Failure(ex) => complete(StatusCodes.BadRequest, "Unable to Save Image; Failed with " + ex )
            }
          }
        }
      } ~ path("images" / LongNumber) { imageId =>
        delete {
          val deleteImages = catalogService.deleteImage(imageId)
          onComplete(deleteImages) {
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, "Unable to Delete Image; Failed with " + ex )
          }

        }
      } ~ path("instanceTypes"/IntNumber) { siteId =>
        get {
          val listOfInstanceFlavors = catalogService.getInstanceFlavor(siteId)
          onComplete(listOfInstanceFlavors) {
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, "Unable to get List; Failed with " + ex)
          }
        }
      }

    }

    val testService = new TestService()
    def testRoute =  path("getTest"/IntNumber){ n =>
      get{
        complete(testService.getTest(n))
      }
    } ~path("saveTestAll"){
      put{ entity(as[Instance]){ test =>
        val cmpl = testService.saveTestAll(test)
        complete(cmpl)
      }
      }
    }

    val nodeService = new NodeService()
    def nodeRoute = pathPrefix("node"){
      path("list"){
        get{
          val listOfAllInstanceNodes = nodeService.getAllNodes
          onComplete(listOfAllInstanceNodes){
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to get Instance nodes; Failed with " + ex )
          }
        }
      } ~ path("topology"){
        get{
          val topology = nodeService.getTopology
          onComplete(topology){
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to get Instance nodes; Failed with " + ex )
          }
        }
      } ~ path(Segment){ name =>
        get {
          val nodeInstance = nodeService.getNode(name)
          onComplete(nodeInstance){
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(ex) => complete(StatusCodes.BadRequest, s"Unable to get Instance with name $name; Failed with " + ex )
          }
        }
      }
    }

    val route = itemRoute ~ orderRoute ~ catalogRoutes ~ nodeRoute ~ testRoute

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}