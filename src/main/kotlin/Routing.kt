package com.example

import com.example.entities.Participant
import com.example.entities.Tournament
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.*

// https://ktor.io/docs/server-create-a-new-project.html#configure-static-content
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get<Articles> { article ->
            // Get all articles ...
            call.respond("List of articles sorted starting from ${article.sort}")
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
        get("/tournament") {
            val tournament = Tournament(
                1L,
                "My tournament",
                "My game",
                1L,
                "Dani",
                MutableList<Participant>(1){ Participant(2L, 2L, "Dani", 3f) },
                32,
                null,
                "Ubicación",
                "Premio",
                123
            )

            call.respond(tournament)
        }
    }
}