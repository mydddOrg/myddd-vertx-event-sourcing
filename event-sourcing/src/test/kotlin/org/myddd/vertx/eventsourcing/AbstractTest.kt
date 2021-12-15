package org.myddd.vertx.eventsourcing

import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.junit5.VertxExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.myddd.vertx.eventsourcing.domain.InventoryCommand
import org.myddd.vertx.id.IDGenerator
import org.myddd.vertx.ioc.InstanceFactory
import java.util.*
import kotlin.random.Random

@ExtendWith(VertxExtension::class,IOCInitExtension::class)
abstract class AbstractTest {

    val logger by lazy { LoggerFactory.getLogger(AbstractTest::class.java) }

    companion object {
        private val idGenerate by lazy { InstanceFactory.getInstance(IDGenerator::class.java) }
    }

    protected fun randomString():String {
        return UUID.randomUUID().toString()
    }

    protected fun randomLong():Long {
        return idGenerate.nextId()
    }

    protected fun randomPurchaseGoodsInventoryCommand(): InventoryCommand.PurchaseGoodsInventoryCommand {
        return InventoryCommand.PurchaseGoodsInventoryCommand(organization = randomString(), goods = randomString(), amount = Random.nextLong(10,20))
    }

    protected fun randomShipGoodsInventoryCommand(): InventoryCommand.ShipGoodsInventoryCommand {
        return InventoryCommand.ShipGoodsInventoryCommand(organization = randomString(), goods = randomString(), amount = Random.nextLong(10,20))
    }

}