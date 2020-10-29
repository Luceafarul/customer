package example.database

import slick.jdbc.JdbcBackend.Database

trait DatabaseService {
  def db: Database
}
