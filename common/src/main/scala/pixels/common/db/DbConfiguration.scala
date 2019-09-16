package pixels.common.db

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object DbConfiguration {
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
}
