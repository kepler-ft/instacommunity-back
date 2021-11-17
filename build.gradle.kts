val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposedVersion : String by project
val pgjdbcVersion : String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
                id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
}

group = "org.kepler42"
version = "0.0.1"
application {
    mainClass.set("org.kepler42.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng:$pgjdbcVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
}