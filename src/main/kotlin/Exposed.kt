package com.example

import com.example.models.LoginModel
import com.example.models.LoginResponse
import com.example.models.NewUserModel
import com.example.models.PasswordModel
import com.example.models.RememberPasswordModel
import com.example.models.UserModel
import com.example.utils.Auth
import com.example.utils.Security
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

suspend fun Application.configureExposed() {
    val database = DatabaseHelper().createConnection()
    DatabaseHelper().createSchema(database)

    val userService = UserService(database)
    val tournamentService = TournamentService(database)

    routing {
        authenticate("auth-jwt") {
            get("/getUser") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId != null) {
                    val user = userService.findUserById(userId.toLong())
                    if (user != null) {
                        call.respond(user)
                    }else call.respond(HttpStatusCode.NotFound)
                }else{ call.respond(HttpStatusCode.Unauthorized) }
            }
        }

        // Create user
        post("/newUser") {
            val user = call.receive<NewUserModel>()
            val id = userService.create(user)
            if(id == -1L) call.respond(HttpStatusCode.BadRequest, UserModel(-1, "", "", -1))
            else call.respond(HttpStatusCode.Created, UserModel(id, user.nickname, user.email, user.photo))
        }

        post("/login"){
            val request = call.receive<LoginModel>()

            val user = userService.findUserForLogin(request.email)
            val matchPassword = Security.checkPasswords(request.password, user?.passwordHashed)
            if(user != null && matchPassword){
                val token = Auth().generateToken(user.id)
                call.respond(
                    LoginResponse(
                        token = token,
                        user = UserModel(user.id, user.nickname, user.email, user.photo)
                    )
                )
            }else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        post("/rememberPassword"){
            val request = call.receive<RememberPasswordModel>()
            val nicknameMatches = userService.getUserNicknameByEmail(request.email) == request.nickname
            if(nicknameMatches) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }
        post("/updatePassword"){
            val request = call.receive<LoginModel>()
            val passwordUpdated = userService.updatePassword(request.email, request.password)
            if(passwordUpdated) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }

        route("/user"){
            authenticate("auth-jwt"){
                post("/checkPassword"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    if(userId == null){
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }
                    val password = call.receive<PasswordModel>().password
                    if(password.isNullOrBlank()){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }
                    if(userService.checkPassword(userId, password)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.Unauthorized)
                }

                post("/updatePassword"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    if(userId == null){
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }
                    val password = call.receive<PasswordModel>().password
                    if(password.isNullOrBlank()){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }
                    val passwordUpdated = userService.updatePassword(userId, password)
                    if(passwordUpdated) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }
                get("/updateAvatar/{avatarId}"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val avatarId = call.parameters["avatarId"]?.toInt()

                    if(userId == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    if(avatarId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val avatarUpdated = userService.updateAvatar(userId, avatarId)
                    if(avatarUpdated) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.Conflict)
                }

                get("/getCreatedTournaments"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    if (userId != null) {
                        val tournamentIdList = userService.getRelationsForUser(userId, "CREATED")
                        val tournaments = tournamentService.getTournamentsById(tournamentIdList)
                        call.respond(tournaments)
                    }else call.respond(HttpStatusCode.NotFound)
                }
                get("/getJoinedTournaments"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    if (userId != null) {
                        val tournamentIdList = userService.getRelationsForUser(userId, "JOINED")
                        val tournaments = tournamentService.getTournamentsById(tournamentIdList)
                        call.respond(tournaments)
                    }else call.respond(HttpStatusCode.NotFound)
                }
                get("/getFollowingTournaments"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    if (userId != null) {
                        val tournamentIdList = userService.getRelationsForUser(userId, "FOLLOWING")
                        val tournaments = tournamentService.getTournamentsById(tournamentIdList)
                        call.respond(tournaments)
                    }else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
