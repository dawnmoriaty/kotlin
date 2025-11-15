val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

group = "com.financial"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    // Exposed (Database ORM) - QUAN TRỌNG
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.47.0")

    // PostgreSQL Driver - QUAN TRỌNG
    implementation("org.postgresql:postgresql:42.7.2")

    // HikariCP (Connection Pool) - QUAN TRỌNG
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Firebase Admin SDK - QUAN TRỌNG
    implementation("com.google.firebase:firebase-admin:9.2.0")

    // MinIO S3 Client - For file/image storage
    implementation("io.minio:minio:8.5.7")

    // Nếu IntelliJ chưa thêm Call Logging
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("at.favre.lib:bcrypt:0.10.2")
}
