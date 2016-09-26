package persistance



import com.activegrid.entities.AuthSettings

import scala.concurrent.Future

/**
  * Created by sivag on 26/9/16.
  */
class AppSettingsManager {


   def persistAuthSettings(authSettings: AuthSettings) : Future[AuthSettings] = Future {
        //Neo4j Operations
        authSettings;
   }


  def getSettings(): Future[List[AuthSettings]] = Future{
      // Neo4j operations
      val authSettings = new AuthSettings("authType","Level","Scope");
      val list: List[AuthSettings] = List(authSettings);
      list;

  }
}
