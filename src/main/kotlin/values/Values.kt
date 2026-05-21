package com.example.values

data class Values(
    val dbUrl : String = System.getenv("DB_URL"),
    val dbUserName : String = System.getenv("DB_USERNAME"),
    val dbUserPassword : String = System.getenv("DB_PASSWORD"),

    val secretKey : String = System.getenv("SECRET_KEY"),
    val expirationTimeToken : String = System.getenv("EXPIRATION_TIME")
)