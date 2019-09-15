package pixels.projection

import akka.actor.typed.ActorSystem
import akka.persistence.query.scaladsl.ReadJournal
import akka.persistence.query._
import akka.persistence.query.scaladsl.PersistenceIdsQuery
import akka.persistence.query.scaladsl.CurrentPersistenceIdsQuery
import akka.persistence.query.scaladsl.EventsByPersistenceIdQuery
import akka.persistence.query.scaladsl.CurrentEventsByTagQuery
import akka.persistence.query.scaladsl.CurrentEventsByPersistenceIdQuery
import akka.persistence.query.scaladsl.EventsByTagQuery
import com.typesafe.config.ConfigFactory
import akka.actor.typed.scaladsl.adapter._

trait PersistenceQuery {
  type RJ = ReadJournal
    with PersistenceIdsQuery
    with CurrentPersistenceIdsQuery
    with EventsByPersistenceIdQuery
    with CurrentEventsByPersistenceIdQuery
    with EventsByTagQuery
    with CurrentEventsByTagQuery

  val configuration = ConfigFactory.load()

  val journalId = configuration.getString("akka.persistence.query.journal.id")

  def readJournal(system: ActorSystem[_]): RJ =
    PersistenceQuery(system.toUntyped).readJournalFor[RJ](journalId)

}
