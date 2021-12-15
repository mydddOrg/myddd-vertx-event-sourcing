plugins {
    java
    kotlin("jvm")
}

group = "org.myddd.vertx.eventsourcing"
version = rootProject.extra["version"]!!

dependencies {
    implementation(kotlin("stdlib"))

    api("org.myddd.vertx:myddd-vertx-domain:${rootProject.extra["myddd_vertx_version"]}")
    api("org.myddd.vertx:myddd-vertx-ioc-api:${rootProject.extra["myddd_vertx_version"]}")
    api("org.myddd.vertx:myddd-vertx-base-api:${rootProject.extra["myddd_vertx_version"]}")

    implementation("org.myddd.vertx:myddd-vertx-base-config:${rootProject.extra["myddd_vertx_version"]}")
    implementation("org.myddd.vertx:myddd-vertx-repository-api:${rootProject.extra["myddd_vertx_version"]}")
    //api
    api("org.myddd.vertx:myddd-vertx-repository-hibernate:${rootProject.extra["myddd_vertx_version"]}")
    //api implementation
    api("org.hibernate.reactive:hibernate-reactive-core:${rootProject.extra["hibernate_reactive_version"]}")
    api("org.myddd.vertx:myddd-vertx-base-provider:${rootProject.extra["myddd_vertx_version"]}")
    implementation("org.apache.logging.log4j:log4j-core:${rootProject.extra["log4j_version"]}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jackson_version"]}")

    api("org.myddd.vertx:myddd-vertx-querychannel-api:${rootProject.extra["myddd_vertx_version"]}")
    implementation("org.myddd.vertx:myddd-vertx-querychannel-hibernate:${rootProject.extra["myddd_vertx_version"]}")

    testImplementation("io.vertx:vertx-mysql-client:${rootProject.extra["vertx_version"]}")
}
