package com.example

import com.example.entities.Participant
import com.example.entities.Tournament
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
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