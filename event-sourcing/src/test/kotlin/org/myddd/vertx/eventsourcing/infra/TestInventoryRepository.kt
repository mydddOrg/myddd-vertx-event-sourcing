package org.myddd.vertx.eventsourcing.infra

import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.myddd.vertx.eventsourcing.AbstractTest
import org.myddd.vertx.eventsourcing.domain.InventoryRepository
import org.myddd.vertx.ioc.InstanceFactory
import org.myddd.vertx.junit.execute

class TestInventoryRepository:AbstractTest() {

    private val inventoryRepository by lazy { InstanceFactory.getInstance(InventoryRepository::class.java) }

    @Test
    fun testQueryMaxSyncId(testContext: VertxTestContext){
        testContext.execute {
            val max = inventoryRepository.queryMaxLastSyncId().await()
            testContext.verify {
                Assertions.assertThat(max).isEqualTo(0)
            }
        }
    }
}