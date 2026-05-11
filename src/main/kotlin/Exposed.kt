package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.LoginModel
import com.example.models.UserModel
import com.example.values.Values
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.jdbc.Database
import java.util.Date

suspend fun Application.configureExposed() {
    val database = Database.connect(
        url = Values().dbUrl,
        driver = "org.mariadb.jdbc.Driver",
        user = Values().dbUserName,
        password = Values().dbUserPassword
    )
    val userService = ExposedUserService(database).also {
        it.createSchema()
    }

    routing {
        // Create user
        post("/newUser") {
            val user = call.receive<NewUserModel>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, UserModel(id.toLong(), user.nickname, user.email, user.photo))
        }

        post("/login"){
            val request = call.receive<LoginModel>()
        }

        // Read user
        /*get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }*/

        // Update user
        /*put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<NewUserModel>()
            userService.update(id, user)
            call.respond(HttpStatusCode.NoContent)
        }*/

        // Delete user
        /*delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }*/

        fun generateToken(email: String): String {
            return JWT.create()
                .withClaim("email", email)
                .withExpiresAt(Date(System.currentTimeMillis() + 172800000)) // 2 días
                .sign(Algorithm.HMAC256(Values().secretKey))
        }
    }
}
