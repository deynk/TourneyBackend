package com.example

import com.example.entities.Participant
import com.example.entities.TournamentModel
import com.example.models.LoginModel
import com.example.models.LoginResponse
import com.example.models.NewUserModel
import com.example.models.PasswordModel
import com.example.models.EmailAndNickname
import com.example.models.IdModel
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
import io.ktor.server.routing.delete
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
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                if (userId != null) {
                    val user = userService.findUserById(userId)
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
            val request = call.receive<EmailAndNickname>()
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
                post("/editAccount"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val emailAndNickname = call.receive<EmailAndNickname>()

                    if(userId == null){
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    val result = userService.editAccount(userId, emailAndNickname)
                    when (result) {
                        0 -> call.respond(HttpStatusCode.OK)
                        1 -> call.respond(HttpStatusCode.Conflict)
                        else -> call.respond(HttpStatusCode.BadRequest)
                    }
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

                post("/followTournament/{id}"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val tournamentId = call.parameters["id"]?.toLongOrNull()

                    if(userId == null || tournamentId == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    if(tournamentService.getTournamentById(tournamentId) == null){
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    if(userService.addTournamentRelation(userId, tournamentId, "FOLLOWING")) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.Conflict)
                }

                post("/unfollowTournament/{id}"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val tournamentId = call.parameters["id"]?.toLongOrNull()

                    if(userId == null || tournamentId == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    if(userService.removeTournamentRelation(userId, tournamentId, "FOLLOWING")) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/joinTournament/{id}"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val tournamentId = call.parameters["id"]?.toLongOrNull()

                    if(userId == null || tournamentId == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    val user = userService.findUserById(userId)
                    if(user == null){
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    val participantAdded = tournamentService.addParticipant(
                        tournamentId,
                        Participant(userId = user.id, nickname = user.nickname)
                    )

                    if(!participantAdded){
                        call.respond(HttpStatusCode.Conflict)
                        return@post
                    }

                    if(userService.addTournamentRelation(userId, tournamentId, "JOINED")) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.Conflict)
                }

                post("/leaveTournament/{id}"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val tournamentId = call.parameters["id"]?.toLongOrNull()

                    if(userId == null || tournamentId == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    tournamentService.removeParticipant(tournamentId, userId)
                    if(userService.removeTournamentRelation(userId, tournamentId, "JOINED")) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }
            }
        }

        route("/tournament"){
            authenticate("auth-jwt"){
                get("/getAllTournaments"){
                    call.respond(tournamentService.getAllTournaments())
                }

                get("/getTournamentByCode/{code}"){
                    val code = call.parameters["code"]?.toIntOrNull()

                    if(code == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val tournament = tournamentService.getTournamentByCode(code)
                    if(tournament != null) call.respond(tournament)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/createTournament"){
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val tournament = call.receive<TournamentModel>()
                    if(userId == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    val tournamentId = tournamentService.insertTournament(tournament)
                    if(tournamentId > 0L) call.respond(HttpStatusCode.OK, IdModel(tournamentId))
                    else call.respond(HttpStatusCode.BadRequest)
                }

                post("/updateTournament"){
                    val tournament = call.receive<TournamentModel>()
                    if(tournamentService.updateTournament(tournament)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/updateParticipants/{id}"){
                    val tournamentId = call.parameters["id"]?.toLongOrNull()
                    val participants = call.receive<List<Participant>>()

                    if(tournamentId == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    if(tournamentService.updateParticipants(tournamentId, participants)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                post("/updateThumbnail/{id}/{thumbnail}"){
                    val tournamentId = call.parameters["id"]?.toLongOrNull()
                    val thumbnail = call.parameters["thumbnail"]?.toIntOrNull()

                    if(tournamentId == null || thumbnail == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }

                    if(tournamentService.updateThumbnail(tournamentId, thumbnail)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }

                delete("/deleteTournament/{id}"){
                    val tournamentId = call.parameters["id"]?.toLongOrNull()

                    if(tournamentId == null){
                        call.respond(HttpStatusCode.BadRequest)
                        return@delete
                    }

                    if(tournamentService.deleteTournament(tournamentId)) call.respond(HttpStatusCode.OK)
                    else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
