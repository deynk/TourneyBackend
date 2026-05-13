package com.example.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse (
    var id: Long,
    var nickname: String,
    var email: String,
    var photo: Int
    //var adminTournamentList: MutableList<Long> = mutableListOf()
)


data class User (
    var id: Long,
    var nickname: String,
    var email: String,
    var photo: Int,
    var showableTournamentList: MutableList<Long> = mutableListOf(),
    var followingTournamentList: MutableList<Long> = mutableListOf(),
    var joinedTournamentList: MutableList<Long> = mutableListOf()
    //var adminTournamentList: MutableList<Long> = mutableListOf()
){
    fun addShowableTournament(id: Long){ showableTournamentList.add(id) }
    fun addFollowingTournament(id: Long){ followingTournamentList.add(id) }
    fun addJoinedTournament(id: Long){ joinedTournamentList.add(id) }
    fun removeShowableTournament(id: Long){ showableTournamentList.remove(id) }
    fun removeFollowingTournament(id: Long){ followingTournamentList.remove(id) }
    fun removeJoinedTournament(id: Long){ joinedTournamentList.remove(id) }
    fun hasShowableTournament(id: Long): Boolean{ return showableTournamentList.contains(id) }
    fun setShowableTournamentList(stringList: String){
        showableTournamentList = stringList.split(",").map { it.trim().toLong() }.toMutableList()
    }

    fun User.toResponse() = UserResponse(
        id = id,
        nickname = nickname,
        email = email,
        photo = photo,
    )
}