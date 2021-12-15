package org.myddd.vertx.eventsourcing.domain

import io.vertx.core.Future
import org.myddd.vertx.domain.BaseAutoIDEntity
import org.myddd.vertx.ioc.InstanceFactory
import javax.persistence.Entity
import javax.persistence.Table

/**
 * 库存事件领域实体，记录所有库存的事件
 */
@Entity
@Table(name = "inventory_event")
class InventoryEvent constructor():BaseAutoIDEntity() {

    lateinit var organization:String

    lateinit var goods:String

    lateinit var eventType: InventoryEventType

    var amount:Long = 0

    constructor(organization:String,goods:String,eventType: InventoryEventType,amount:Long):this(){
        this.organization = organization
        this.goods = goods
        this.eventType = eventType
        this.amount = amount
    }

    companion object {
        private val inventoryRepository by lazy { InstanceFactory.getInstance(InventoryRepository::class.java) }

        suspend fun batchAddEvent(events:Array<InventoryEvent>):Future<Boolean>{
            return inventoryRepository.batchSave(events)
        }
    }

    suspend fun addEvent():Future<InventoryEvent>{
        return inventoryRepository.save(this)
    }

}