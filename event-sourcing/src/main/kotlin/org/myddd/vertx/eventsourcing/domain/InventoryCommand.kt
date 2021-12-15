package org.myddd.vertx.eventsourcing.domain

import java.io.Serializable

sealed interface InventoryCommand: Serializable {

    /**
     * 进货命令
     */
    data class PurchaseGoodsInventoryCommand(val organization:String, val goods:String, val amount:Long):InventoryCommand{
        fun toInventoryEvent() = InventoryEvent(organization = organization, goods = goods, eventType = InventoryEventType.PURCHASE_GOODS, amount = amount)
    }

    /**
     * 出货命令
     */
    data class ShipGoodsInventoryCommand(val organization:String, val goods:String, val amount:Long):InventoryCommand{
        fun toInventoryEvent() = InventoryEvent(organization = organization, goods = goods, eventType = InventoryEventType.SHIP_GOODS, amount = amount)
    }

    /**
     * 货物调配，从一个组织调配到另一个组织
     */
    data class TransferGoodsInventoryCommand(val fromOrganization:String, val toOrganization:String, val goods:String, val amount:Long):InventoryCommand{
        fun toInventoryEvent() = arrayOf(
                InventoryEvent(organization = fromOrganization, goods = goods, eventType = InventoryEventType.TRANSFER_GOODS_FROM, amount = amount),
                InventoryEvent(organization = toOrganization, goods = goods, eventType = InventoryEventType.TRANSFER_GOODS_TO, amount = amount),
            )
    }
}