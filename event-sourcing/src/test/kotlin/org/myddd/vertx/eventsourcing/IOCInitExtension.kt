package org.myddd.vertx.eventsourcing

import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.vertx.core.Vertx
import org.hibernate.reactive.mutiny.Mutiny
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.myddd.vertx.eventsourcing.domain.InventoryRepository
import org.myddd.vertx.eventsourcing.infra.InventoryRepositoryHibernate
import org.myddd.vertx.id.IDGenerator
import org.myddd.vertx.id.SnowflakeDistributeId
import org.myddd.vertx.id.StringIDGenerator
import org.myddd.vertx.id.ULIDStringGenerator
import org.myddd.vertx.ioc.InstanceFactory
import org.myddd.vertx.ioc.guice.GuiceInstanceProvider
import org.myddd.vertx.string.RandomIDString
import org.myddd.vertx.string.RandomIDStringProvider
import javax.persistence.Persistence

class IOCInitExtension:BeforeAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        InstanceFactory.setInstanceProvider(GuiceInstanceProvider(Guice.createInjector(object : AbstractModule(){
            override fun configure() {
                bind(Vertx::class.java).toInstance(Vertx.vertx())
                bind(Mutiny.SessionFactory::class.java).toInstance(
                    Persistence.createEntityManagerFactory("default")
                        .unwrap(Mutiny.SessionFactory::class.java))
                bind(IDGenerator::class.java).toInstance(SnowflakeDistributeId())
                bind(StringIDGenerator::class.java).to(ULIDStringGenerator::class.java)

                bind(RandomIDString::class.java).to(RandomIDStringProvider::class.java)

                bind(InventoryRepository::class.java).to(InventoryRepositoryHibernate::class.java)
            }
        })))
    }
}