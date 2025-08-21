package com.app.gdl.data.model

data class OrderRequest(
    val customer_id: Int,
    val customer_name: String,
    val customer_address: String,
    val customer_city: String,
    val customer_phone: String,
    val warehouse: String,
    val price_class: String,
    val order_date: String,
    val order_details: List<OrderItem>
)

data class OrderItem(
    val InventoryID: String,
    val ProductName: String,
    val OrderQty: Int,
    val UnitPrice: Double
)

data class OrderResponse(
    val status: Int,
    val cfa_order_id: String,
    val acu_order_number: String,
    val message: String
)

