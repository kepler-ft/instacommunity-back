package org.kepler42

import io.ktor.server.engine.*
import io.ktor.server.netty.*

import org.kepler42.plugins.*
import org.kepler42.database.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureHTTP()
        configureDatabase()
    }.start(wait = true)
}
