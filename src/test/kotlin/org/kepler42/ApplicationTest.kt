package org.kepler42

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlin.test.*
import io.ktor.server.testing.*

import org.kepler42.plugins.*
import org.kepler42.models.Greeting

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class ApplicationTest {
    @Test
    @kotlinx.serialization.ExperimentalSerializationApi
    fun testRoot() {
        withTestApplication({ configureRouting(); configureSerialization() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    Json.encodeToString(Greeting(1, "Hello, Kepler!")),
                    response.content
                )
            }
        }
    }
}
