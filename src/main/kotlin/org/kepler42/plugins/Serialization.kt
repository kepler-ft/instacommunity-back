package org.kepler42.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json( Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }
}
