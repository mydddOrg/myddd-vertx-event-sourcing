package org.myddd.vertx.eventsourcing.domain

import io.vertx.core.Future
import org.myddd.vertx.repository.api.EntityRepository

interface InventoryRepository:EntityRepository {

    suspend fun queryMaxLastSyncId():Future<Long>
    suspend fun queryAdditionalInventoryEvents():Future<List<InventoryEvent>>
}