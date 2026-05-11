package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.values.Values
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureAuth() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "tourney"

            verifier(
                JWT
                    .require(Algorithm.HMAC256(Values().secretKey))
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}