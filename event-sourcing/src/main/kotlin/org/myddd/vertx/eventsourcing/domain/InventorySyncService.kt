package org.myddd.vertx.eventsourcing.domain

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.myddd.vertx.ioc.InstanceFactory
import java.util.*

object InventorySyncService {


    private val inventoryRepository by lazy { InstanceFactory.getInstance(InventoryRepository::class.java) }

    private val vertx by lazy { InstanceFactory.getInstance(Vertx::class.java) }

    private val logger by lazy { LoggerFactory.getLogger(InventorySyncService::class.java) }

    init {
        vertx.eventBus().consumer<JsonObject>(InventoryCommandService.INVENTORY_EVENT_NOTICE){
            logger.debug("收到消息，触发状态同步")
            GlobalScope.launch(vertx.dispatcher()) {
                syncInventorySnapshot().await()
            }
        }
    }

    suspend fun syncInventorySnapshot():Future<Unit>{
        val shareData  = vertx.sharedData()
        val lock = shareData.getLock("InventorySyncService").await()
        try {
            val events = inventoryRepository.queryAdditionalInventoryEvents().await()
            events.forEach { event ->
                logger.debug("同步事件：ID:${event.id},组织：${event.organization},货物:${event.goods},事件:${event.eventType},数量:${event.amount}")
                var snapshot = InventorySnapshot.queryInventorySnapshot(organization = event.organization, goods = event.goods).await()
                if(Objects.isNull(snapshot)) snapshot = InventorySnapshot(organization = event.organization, goods = event.goods)
                snapshot!!.processEvent(event).await()
            }
        }finally {
            lock.release()
        }
        return Future.succeededFuture()
    }
}