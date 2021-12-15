package org.myddd.vertx.eventsourcing.domain

import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await
import org.myddd.vertx.domain.BaseStringIDEntity
import org.myddd.vertx.ioc.InstanceFactory
import java.util.*
import javax.persistence.*

/**
 * 库存信息快照，这个快照保存的是最新的库存信息
 */
@Entity
@Table(name = "inventory_snapshot_", uniqueConstraints = [
    UniqueConstraint(name = "unique_snapshot_organization_goods", columnNames = ["organization","goods"])
],
    indexes = [
        Index(name = "index_last_sync_id", columnList = "last_sync_id")
    ]
)
class InventorySnapshot constructor(): BaseStringIDEntity() {

    lateinit var organization:String

    lateinit var goods:String

    var amount:Long = 0

    @Column(name = "last_sync_id", nullable = false)
    var lastSyncId:Long = 0

    constructor(organization:String,goods:String):this(){
        this.organization = organization
        this.goods = goods
    }

    companion object {
        private val inventoryRepository by lazy { InstanceFactory.getInstance(InventoryRepository::class.java) }

        suspend fun queryInventorySnapshot(organization:String,goods:String):Future<InventorySnapshot?>{
            return inventoryRepository.singleQuery(
                clazz = InventorySnapshot::class.java,
                sql = "from InventorySnapshot where organization = :organization and goods = :goods",
                params = mapOf(
                    "organization" to organization,
                    "goods" to goods
                )
            )
        }

        suspend fun checkAmountEnough(organization:String,goods:String,exceptedAmount:Long):Future<Boolean>{
            val snapshot = queryInventorySnapshot(organization,goods).await()
            val storageAmount = if(Objects.isNull(snapshot)) 0L else snapshot!!.amount
            return Future.succeededFuture(storageAmount >= exceptedAmount)
        }
    }

    suspend fun processEvent(event: InventoryEvent):Future<InventorySnapshot>{
        check(event.organization == this.organization){"组织不一致，无法处理此事件"}
        check(event.goods == this.goods){"商品名称不一致，无法处理此事件"}

        when(event.eventType){
            InventoryEventType.PURCHASE_GOODS -> this.amount += event.amount
            InventoryEventType.SHIP_GOODS -> this.amount -= event.amount
            InventoryEventType.TRANSFER_GOODS_FROM -> this.amount -= event.amount
            InventoryEventType.TRANSFER_GOODS_TO -> this.amount += event.amount
        }
        this.lastSyncId = event.id
        return inventoryRepository.save(this)
    }
}