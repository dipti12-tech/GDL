package com.app.gdl.data.model

data class LoginRequest(
    val uname: String,
    val pwd: String,
    val fcm_token:String
)