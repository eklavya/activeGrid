# Learning akka for web application (ActiveGrid)

Defining the server startup code,

1. Main is the initial class which providing server configuration

  ```scala 
  object Main extends App{
  }
  ```
  ---
  Extending Main class with trait App to execute all the statements availble in it.

1. Initializing actor system 
  ```scala
  implicit val system = ActorSystem()
  ```

1. Application Configuration 

  Conf properties maintained in the Application.conf
  to access the application configuration
  ```scala
  val config = system.settings.config
  val interface = config.getString("http.interface")
  ```
1. Execution context
  In Akka all the Future and Actor execution expect context
    
  ```scala
  implicit val executionContext = system.dispatcher
  ```
1. Passing execution context to the Resources to execute Future. All the Api classes group in the resouces package.
  
  ```scala
  class ItemResource (implicit executionContext: ExecutionContext)
  ```
  ItemResouce class expecting executionContext as implicit object. It means we need not to pass the value explicitly. Scala will find the best suited value from the context.
  ```scala
  val itemRoute = new ItemResource().route
  ```



