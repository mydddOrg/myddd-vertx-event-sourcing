package org.myddd.vertx.eventsourcing.api

import io.vertx.core.Future
import org.myddd.vertx.eventsourcing.api.command.InventoryCommandDto

interface InventoryApplication {

    /**
     * 处理各种命令
     */
    suspend fun processCommand(inventoryCommandDto: InventoryCommandDto):Future<Unit>

}