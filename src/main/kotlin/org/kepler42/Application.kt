package org.kepler42

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.kepler42.database.*
import org.kepler42.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureInjection()
        configureRouting()
        configureSerialization()
        configureHTTP()
        configureDatabase()
        configureFirebase()
    } .start(wait = true)
}
