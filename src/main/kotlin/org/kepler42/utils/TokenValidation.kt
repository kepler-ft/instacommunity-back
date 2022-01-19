package org.kepler42.utils

import com.google.firebase.auth.FirebaseAuth
import io.ktor.application.*
import io.ktor.request.*
import org.kepler42.errors.UnauthorizedException

class TokenValidator {
    fun checkAuth(call: ApplicationCall): String {
        val token = call.request.header("Authorization") ?: throw UnauthorizedException("Missing Authorization header")
        val idToken = if (token.startsWith("Bearer "))
            token.split(" ")[1]
        else
            throw UnauthorizedException("Invalid Authorization header")
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
        return decodedToken.uid
    }
}
