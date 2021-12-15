package org.myddd.vertx.eventsourcing.domain

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import org.myddd.vertx.eventsourcing.InventoryShortageException
import org.myddd.vertx.ioc.InstanceFactory

/**
 * 领域层中，处理command的领域服务
 */
object InventoryCommandService {

    const val INVENTORY_EVENT_NOTICE = "INVENTORY_EVENT_NOTICE"

    private val vertx by lazy { InstanceFactory.getInstance(Vertx::class.java) }

    /**
     * 处理库存相关事件，当前支持，进货，出货，以及调拨三个事件
     */
    suspend fun processInventoryCommand(inventoryCommand: InventoryCommand):Future<Unit>{
        val result =  when(inventoryCommand){
            is InventoryCommand.PurchaseGoodsInventoryCommand -> processPurchaseGoodsInventoryCommand(inventoryCommand)
            is InventoryCommand.ShipGoodsInventoryCommand -> processShipGoodsInventoryCommand(inventoryCommand)
            is InventoryCommand.TransferGoodsInventoryCommand -> processTransferGoodsInventoryCommand(inventoryCommand)
        }
        //发送消息，触发状态同步
        vertx.eventBus().send(INVENTORY_EVENT_NOTICE,JsonObject.mapFrom(inventoryCommand))
        return result
    }

    /**
     * 处理进货事件
     */
    private suspend fun processPurchaseGoodsInventoryCommand(command: InventoryCommand.PurchaseGoodsInventoryCommand):Future<Unit>{
        check(command.amount > 0){"进货商品数量必须大于0"}
        val inventoryEvent = command.toInventoryEvent()
        inventoryEvent.addEvent().await()
        return Future.succeededFuture()
    }

    /**
     * 处理出货事件
     */
    private suspend fun processShipGoodsInventoryCommand(command: InventoryCommand.ShipGoodsInventoryCommand):Future<Unit>{
        check(command.amount > 0){"出货商品数量必须大于0"}
        //出货前，检查对应组织的此商品的库存是否充足
        val storageEnough = InventorySnapshot.checkAmountEnough(organization = command.organization, goods = command.goods, exceptedAmount = command.amount).await()
        if(!storageEnough) throw InventoryShortageException()

        //充足的情况下，产生出库行为
        val inventoryEvent = command.toInventoryEvent()
        inventoryEvent.addEvent().await()
        return Future.succeededFuture()
    }

    /**
     * 处理调波事件
     */
    private suspend fun processTransferGoodsInventoryCommand(command: InventoryCommand.TransferGoodsInventoryCommand):Future<Unit>{
        //调拨前，检查调出组织的此商品库存是否充足
        val storageEnough = InventorySnapshot.checkAmountEnough(organization = command.fromOrganization, goods = command.goods, exceptedAmount = command.amount).await()
        if(!storageEnough) throw InventoryShortageException()

        val inventoryEventList = command.toInventoryEvent()
        InventoryEvent.batchAddEvent(inventoryEventList).await()
        return Future.succeededFuture()
    }

}