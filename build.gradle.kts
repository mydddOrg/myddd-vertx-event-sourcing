import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra["kotlinVersion"] = "1.6.0"

    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
    }
}

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.6.0"
    id("jacoco")
    id("org.sonarqube") version "3.3"
}

val projectVersion = "1.0.0-SNAPSHOT"

extra["version"] = projectVersion
extra["myddd_vertx_version"] = "1.3.0-SNAPSHOT"

extra["kotlin_version"] = "1.6.0"
extra["vertx_version"] = "4.2.1"
extra["hibernate_reactive_version"] = "1.1.0.Final"

extra["log4j_version"] = "2.14.1"
extra["jackson_version"] = "2.13.0"
extra["javax_persistence_version"] = "2.2.1"
extra["mockito_version"] = "4.0.0"
extra["commons_lang3_version"] = "3.12.0"
extra["junit5_version"] = "5.7.1"

extra["protobuf-java"] = "3.17.3"
extra["annotation-api"] = "1.3.2"
extra["assertj-core-version"] = "3.21.0"
group = "org.myddd.vertx"
version = projectVersion


allprojects {
    // don't cache changing modules at all
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }
}


subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "org.sonarqube")

    jacoco {
        toolVersion = "0.8.7"
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
    }

    dependencies{

        implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlin_version"]}")


        implementation("io.vertx:vertx-core:${rootProject.extra["vertx_version"]}")
        implementation("io.vertx:vertx-lang-kotlin:${rootProject.extra["vertx_version"]}")
        implementation("io.vertx:vertx-lang-kotlin-coroutines:${rootProject.extra["vertx_version"]}")
        implementation("org.myddd.vertx:myddd-vertx-ioc-api:${rootProject.extra["myddd_vertx_version"]}")

        testImplementation("org.myddd.vertx:myddd-vertx-ioc-guice:${rootProject.extra["myddd_vertx_version"]}")
        testImplementation("org.myddd.vertx:myddd-vertx-base-provider:${rootProject.extra["myddd_vertx_version"]}")
        testImplementation("org.myddd.vertx:myddd-vertx-junit:${rootProject.extra["myddd_vertx_version"]}")
        testImplementation("org.apache.logging.log4j:log4j-core:${rootProject.extra["log4j_version"]}")
    }
}


sonarqube {
    properties {
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src")
        property("sonar.language","kotlin")
        property("sonar.sources","src/main/kotlin")
    }
}

allprojects {
    repositories {
        maven {
            setUrl("https://maven.myddd.org/releases/")
        }
        maven {
            setUrl("https://maven.myddd.org/snapshots/")
        }

        mavenCentral()

        maven {
            setUrl("https://jitpack.io")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.jar {
    enabled = true
}
