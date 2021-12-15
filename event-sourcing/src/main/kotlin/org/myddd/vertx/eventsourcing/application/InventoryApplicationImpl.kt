package org.myddd.vertx.eventsourcing.application

import io.vertx.core.Future
import org.myddd.vertx.eventsourcing.api.InventoryApplication
import org.myddd.vertx.eventsourcing.api.command.InventoryCommandDto

class InventoryApplicationImpl:InventoryApplication {
    override suspend fun processCommand(inventoryCommandDto: InventoryCommandDto): Future<Unit> {
        TODO("Not yet implemented")
    }
}