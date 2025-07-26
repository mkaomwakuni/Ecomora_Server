import org.apache.tools.ant.filters.ReplaceTokens
import java.net.InetAddress
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val hikaricp_version: String by project
val exposed_version: String by project
val kafka_version: String by project
val micrometer_version: String by project
val redis_version: String by project

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow")
}
tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "est.ecomora.server.ApplicationKt"
    }
}

group = "est.ecomora.server"
version = "1.0.0"

application {
    mainClass.set("est.ecomora.server.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${isDevelopment}")
}


tasks.processResources {
    from(sourceSets["main"].resources.srcDirs)
    into(layout.buildDirectory.dir("upload/products"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    filesMatching("application.conf") {
        filter(
            ReplaceTokens::class, "tokens" to mapOf(
                "BUILD_VERSION" to version,
                "BUILD_DATE" to DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now()),
                "BUILD_MACHINE" to InetAddress.getLocalHost().hostName
            )
        )
    }
}
kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-sessions-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("com.h2database:h2:2.2.220")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-caching-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("com.zaxxer:HikariCP:$hikaricp_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:$postgres_version")
    
    // Kafka dependencies
    implementation("org.apache.kafka:kafka-clients:$kafka_version")
    implementation("org.apache.kafka:kafka-streams:$kafka_version")
    
    // Micrometer dependencies
    implementation("io.micrometer:micrometer-core:$micrometer_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometer_version")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktor_version")
    
    // Redis dependencies
    implementation("redis.clients:jedis:$redis_version")
    
    // Security dependencies
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")

}
tasks.create("stage") {
    dependsOn("installDist")
}
