package com.app.gdl.data.model

data class LoginResponse(
    val status: Int,
    val msg: String,
    val customer_details: CustomerDetails?
)



data class CustomerDetails(
    val customer_id: Int,
    val user_name: String,
    val first_name: String,
    val last_name: String,
    val email_id: String,
    val phone: String,
    val address: List<AddressDetails>?
)

data class AddressDetails(
    val name: String,
    val text: String,
    val default: Int,
    val map_location: List<Double>,
    val warehouse:String,
    val price_class:String
)
