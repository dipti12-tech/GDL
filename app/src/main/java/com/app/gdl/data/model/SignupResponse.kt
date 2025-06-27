package com.app.gdl.data.model

data class SignupResponse(
    val status: Int,
    val message: String,
    val user_id: Int?
)
data class SignupRequest(
    val first_name: String,
    val last_name: String,
    val email_id: String,
    val phone: String,
    val password: String,
    val fcm_token: String,
    val address: List<Address>
)

data class Address(
    val name: String,
    val text: String,
    val map_location: List<Double>,
    val `default`: Int
)