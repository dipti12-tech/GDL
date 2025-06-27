package com.app.gdl.data.model

data class CartItem(
    val inventoryId: String,
    val name: String,
    val imageUrl: String,
    val unit: String,         // e.g., "carton", "piece"
    var quantity: Int,
    val pricePerUnit: Double,  // price for 1 unit
    val category: String
)
