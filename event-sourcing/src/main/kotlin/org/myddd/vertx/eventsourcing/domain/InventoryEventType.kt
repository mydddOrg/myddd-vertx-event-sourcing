package org.myddd.vertx.eventsourcing.domain

/**
 * 库存事件类型
 */
enum class InventoryEventType {

    PURCHASE_GOODS,

    SHIP_GOODS,

    TRANSFER_GOODS_FROM,

    TRANSFER_GOODS_TO

}