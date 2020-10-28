package example.config

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
  private val dbConfig = config.getConfig("database")

  val dbUrl: String = dbConfig.getString("url")
  val dbUser: String = dbConfig.getString("user")
  val dbPassword: String = dbConfig.getString("password")
}
