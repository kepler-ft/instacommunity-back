package org.kepler42.routes

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*

import org.kepler42.models.*
import org.kepler42.database.operations.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Route.communityRoute() {
	    route("/communities") {
        	post ("{id}/followers") {
				try {
					val user = call.receive<User>()
					
					val communityId = call.parameters["id"]
					val response: UserCommunity = insertUsersCommunities(UserCommunity(
						userId = user.id,
						communityId = communityId!!.toInt()))
					call.respond(response)
				} catch (e: ExposedSQLException) {
					call.respond(mapOf("error" to "Deu ruim"))
					TODO("Fazer o select antes de inserir")
				}
			}
    }
}