package com.example

import io.ktor.server.application.*
import io.ktor.server.http.content.default
import io.ktor.server.http.content.staticResources
import io.ktor.server.resources.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureResources() {
    install(Resources)
}

fun Application.module() {
    routing {
        staticResources("/", "static") {
            default("index.html")
        }

        get("/api/hello") {
            call.respondText("Hello from API!")
        }
    }
}