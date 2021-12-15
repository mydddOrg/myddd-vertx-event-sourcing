package org.myddd.vertx.eventsourcing.infra

import io.smallrye.mutiny.Uni
import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await
import org.myddd.vertx.eventsourcing.domain.InventoryEvent
import org.myddd.vertx.eventsourcing.domain.InventoryRepository
import org.myddd.vertx.repository.hibernate.EntityRepositoryHibernate
import java.util.*

class InventoryRepositoryHibernate:EntityRepositoryHibernate(),InventoryRepository {

    override suspend fun queryMaxLastSyncId(): Future<Long> {

        val maxSyncIdRecord = inQuery { session ->
            session.createQuery<Long>("select max(lastSyncId) as max from InventorySnapshot").singleResultOrNull
                .chain { t->
                    if(Objects.isNull(t))Uni.createFrom().item(0L)
                    else Uni.createFrom().item(t)
                }
        }.await()

        return if(Objects.isNull(maxSyncIdRecord)) Future.succeededFuture(0L) else Future.succeededFuture(maxSyncIdRecord)
    }

    override suspend fun queryAdditionalInventoryEvents(): Future<List<InventoryEvent>> {
        return inQuery { session ->
            session.createQuery<Long>("select max(lastSyncId) as max from InventorySnapshot").singleResultOrNull
                .chain { t->
                    if(Objects.isNull(t))Uni.createFrom().item(0L)
                    else Uni.createFrom().item(t)
                }
                .chain { lastMaxId ->
                    session.createQuery("from InventoryEvent where id > :lastMaxId order by id asc",InventoryEvent::class.java)
                        .setParameter("lastMaxId",lastMaxId)
                        .resultList
                }
        }
    }
}