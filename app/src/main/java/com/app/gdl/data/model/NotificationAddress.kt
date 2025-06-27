package com.app.gdl.data.model

    data class NotificationAddress(
        val name: String,
        val text: String,
        val map_location: List<Double>,
        val default: Int,
        val verified: Int,
        val warehouse: String,
        val price_class: String
    )

    data class User(
        val customer_id: Int,
        val customer_name: String,
        val email_id: String,
        val phone: String,
        val fcm_token: String,
        val address: List<NotificationAddress>
    )
