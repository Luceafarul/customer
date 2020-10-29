package example.database

import slick.jdbc.JdbcBackend.Database

class PostgresService extends DatabaseService {
  override def db: Database = Database.forConfig("database")

  db.createSession()
}
