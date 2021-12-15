package org.myddd.vertx.eventsourcing.api.command

import java.io.Serializable

sealed interface InventoryCommandDto: Serializable {

    /**
     * 进货
     */
    data class PurchaseGoodsInventoryCommandDto(val organization:String, val goods:String, val amount:Long):InventoryCommandDto

    /**
     * 出货
     */
    data class ShipGoodsInventoryCommandDto(val organization:String, val goods:String, val amount:Long):InventoryCommandDto

    /**
     * 货物调配，从一个组织调配到另一个组织
     */
    data class TransferGoodsInventoryCommandDto(val fromOrganization:String, val toOrganization:String, val goods:String, val amount:Long):InventoryCommandDto
}