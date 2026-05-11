package com.example.tables

import org.jetbrains.exposed.v1.core.Table

object __UserTable : Table() {
    val id = integer("id").autoIncrement()
    val nickname = varchar("name", 255)
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255)
    val photo = integer("photo")

    override val primaryKey = PrimaryKey(id)
}