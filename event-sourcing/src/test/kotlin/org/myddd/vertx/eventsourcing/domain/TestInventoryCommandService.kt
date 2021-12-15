package org.myddd.vertx.eventsourcing.domain

import io.vertx.core.Future
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.myddd.vertx.eventsourcing.AbstractTest
import org.myddd.vertx.eventsourcing.InventoryShortageException
import org.myddd.vertx.ioc.InstanceFactory
import org.myddd.vertx.junit.assertNotThrow
import org.myddd.vertx.junit.assertThrow
import org.myddd.vertx.junit.execute
import kotlin.random.Random

class TestInventoryCommandService:AbstractTest() {

    private val inventoryRepository by lazy { InstanceFactory.getInstance(InventoryRepository::class.java) }

    /**
     * 进货逻辑测试
     */
    @Test
    fun testProcessPurchaseCommand(testContext: VertxTestContext){
        testContext.execute {
            //进货
            val command = randomPurchaseGoodsInventoryCommand()
            testContext.assertNotThrow {
                InventoryCommandService.processInventoryCommand(command).await()
            }

            //进货后，检查进货事件不为空
            val queryEvent = inventoryRepository.listQuery(
                clazz = InventoryEvent::class.java,
                sql = "from InventoryEvent where organization = :organization and goods = :goods and eventType = :eventType",
                params = mapOf(
                    "organization" to command.organization,
                    "goods" to command.goods,
                    "eventType" to InventoryEventType.PURCHASE_GOODS
                )
            ).await()

            testContext.verify {
                Assertions.assertThat(queryEvent).isNotEmpty
            }

            //同步状态,因为事件与状态是分开且异步的
            syncEventToSnapshot().await()

            //检查库存快照
            var snapshot = InventorySnapshot.queryInventorySnapshot(organization = command.organization, goods = command.goods).await()
            testContext.verify {
                Assertions.assertThat(snapshot).isNotNull
                Assertions.assertThat(snapshot!!.amount).isEqualTo(command.amount)
            }

            //再次进货同一个商品
            val anotherCommand = InventoryCommand.PurchaseGoodsInventoryCommand(organization = command.organization, goods = command.goods, amount = Random.nextLong(10,20))
            testContext.assertNotThrow {
                InventoryCommandService.processInventoryCommand(anotherCommand).await()
            }
            //同步状态,因为事件与状态是分开且异步的
            InventorySyncService.syncInventorySnapshot().await()

            //检查库存快照数为两次进货之和
            snapshot = InventorySnapshot.queryInventorySnapshot(organization = command.organization, goods = command.goods).await()
            testContext.verify {
                Assertions.assertThat(snapshot).isNotNull
                Assertions.assertThat(snapshot!!.amount).isEqualTo(command.amount + anotherCommand.amount)
            }
        }
    }

    /**
     * 出货逻辑测试
     */
    @Test
    fun testProcessShipGoodsInventoryCommand(testContext: VertxTestContext){
        testContext.execute {
            //库存不足，报错
            val shipGoodsInventoryCommand = randomShipGoodsInventoryCommand()
            testContext.assertThrow(InventoryShortageException::class.java){
                InventoryCommandService.processInventoryCommand(shipGoodsInventoryCommand).await()
            }

            //进货，以使商品的库存充足
            val purchaseGoodsInventoryCommand = InventoryCommand.PurchaseGoodsInventoryCommand(organization = shipGoodsInventoryCommand.organization, goods = shipGoodsInventoryCommand.goods, amount = shipGoodsInventoryCommand.amount + 1)
            InventoryCommandService.processInventoryCommand(purchaseGoodsInventoryCommand).await()

            //同步状态,因为事件与状态是分开且异步的
            syncEventToSnapshot().await()

            //库存充足，不会报错
            testContext.assertNotThrow {
                InventoryCommandService.processInventoryCommand(shipGoodsInventoryCommand).await()
            }

            //同步状态,因为事件与状态是分开且异步的
            syncEventToSnapshot().await()

            //一进一出，此商品的库存只有1
            val snapshot = InventorySnapshot.queryInventorySnapshot(organization = shipGoodsInventoryCommand.organization, goods = shipGoodsInventoryCommand.goods).await()
            testContext.verify {
                Assertions.assertThat(snapshot).isNotNull
                Assertions.assertThat(snapshot!!.amount).isEqualTo(1)
            }
        }
    }

    /**
     * 调拨逻辑测试
     */
    @Test
    fun testProcessTransferGoodsInventoryCommand(testContext: VertxTestContext){
        testContext.execute {
            //货物调出公司
            val fromOrganization = randomString()
            //货物调入公司
            val toOrganization = randomString()
            //货物
            val goods = randomString()
            //调出货物数
            val amount = randomLong()

            val randomTransferGoodsInventoryCommand = InventoryCommand.TransferGoodsInventoryCommand(fromOrganization = fromOrganization,toOrganization = toOrganization,goods = goods, amount = amount)

            //调出公司没有此货物，报货物库存不足的错误
            testContext.assertThrow(InventoryShortageException::class.java){
                InventoryCommandService.processInventoryCommand(randomTransferGoodsInventoryCommand).await()
            }

            //给货物调出公司进货，以保证其有足够的货物调出，进货数 = 调出数 + 100
            val randomPurchaseGoodsInventoryCommand = InventoryCommand.PurchaseGoodsInventoryCommand(organization = fromOrganization,goods = goods,amount = amount + 100)
            testContext.assertNotThrow {
                InventoryCommandService.processInventoryCommand(randomPurchaseGoodsInventoryCommand).await()
            }

            //同步状态,因为事件与状态是分开且异步的
            syncEventToSnapshot().await()

            //由于有货，所以调货正常，不会报错
            testContext.assertThrow(InventoryShortageException::class.java){
                InventoryCommandService.processInventoryCommand(randomTransferGoodsInventoryCommand).await()
            }

            //同步状态,因为事件与状态是分开且异步的
            syncEventToSnapshot().await()

            //查询货物调出公司的库存，数额应为100
            val fromSnapshot = InventorySnapshot.queryInventorySnapshot(organization = fromOrganization,goods = goods).await()
            testContext.verify {
                Assertions.assertThat(fromSnapshot).isNotNull
                Assertions.assertThat(fromSnapshot!!.amount).isEqualTo(100)
            }

            //查询货物调入公司的库存，数额应为上述的amount变量值
            val toSnapshot = InventorySnapshot.queryInventorySnapshot(organization = toOrganization,goods = goods).await()
            testContext.verify {
                Assertions.assertThat(toSnapshot).isNotNull
                Assertions.assertThat(toSnapshot!!.amount).isEqualTo(amount)
            }
        }
    }

    private suspend fun syncEventToSnapshot():Future<Unit>{
        return InventorySyncService.syncInventorySnapshot()
    }
}