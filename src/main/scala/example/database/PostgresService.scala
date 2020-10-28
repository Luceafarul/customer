package example.database

import slick.jdbc.PostgresProfile.api._

class PostgresService {
  def db: Database = Database.forConfig("database")

  db.createSession()
}
