package org.kepler42.plugins

import io.ktor.application.*
import io.ktor.routing.*
import org.kepler42.routes.communityRoute
import org.kepler42.routes.moderatorsRoutes
import org.kepler42.routes.tagRoute
import org.kepler42.routes.userRoute

fun Application.configureRouting() {
    routing {
        communityRoute()
        moderatorsRoutes()
        userRoute()
        tagRoute()
    }
}
