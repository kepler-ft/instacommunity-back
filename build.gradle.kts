val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposedVersion : String by project
val pgjdbcVersion : String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
    id("org.flywaydb.flyway") version "8.2.1"
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

val spek_version = "2.0.17"
val koin_version = "3.1.4"
val flywayVersion = "8.2.1"
val kotest_version = "5.0.1"
val mockk_version = "1.12.1"
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

    // spek
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spek_version")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spek_version")
    testImplementation("io.kotest:kotest-assertions-core:$kotest_version")
    testImplementation("io.mockk:mockk:$mockk_version")

    // spek requires kotlin-reflect, can be omitted if already in the classpath
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    // Koin Core features
    implementation("io.insert-koin:koin-core:$koin_version")
    // Koin Test features
    testImplementation("io.insert-koin:koin-test:$koin_version")
    // Koin for Ktor 
    implementation("io.insert-koin:koin-ktor:$koin_version")
    // SLF4J Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("org.postgresql:postgresql:42.3.1")

    implementation("com.google.firebase:firebase-admin:8.1.0")
}

configurations {
    testImplementation.get().exclude("org.jetbrains.kotlin", "kotlin-test-junit")
}

val dbServer: String = System.getenv("DB_SERVER") ?: "ic-postgres"
val dbName: String = System.getenv("DB_NAME") ?: "ic-postgres"
val dbUser: String = System.getenv("DB_USER") ?: "ic-postgres"
val dbPassword: String = System.getenv("DB_PASSWORD") ?: "ic-postgres"
flyway {
    url = "jdbc:postgresql://$dbServer:5432/$dbName"
    user = dbUser
    password = dbPassword
}

// setup the test task
tasks {
    withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }
}
