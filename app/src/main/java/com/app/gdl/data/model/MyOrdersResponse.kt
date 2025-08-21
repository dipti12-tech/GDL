package com.app.gdl.data.model

data class MyOrdersResponse(
    val status: Int,
    val list: List<Order>
)

data class Order(
    val order_id: Int,
    val erp_order_number: String?,
    val customer_id: Int,
    val erp_customer_id: String,
    val customer_name: String,
    val order_desc: String,
    val order_date: String,
    val order_details: List<OrderDetail>,
    val shipping_details: ShippingDetails,
    val order_status: Int,
    val erp_order_status: String,
    val order_total: Any?, // Use Double? if you expect numerical value
    val payment_status: Any?,
    val invoice_id: Any?,
    val status: Int,
    val created_at: String,
    val updated_at: String
)

data class OrderDetail(
    val OrderQty: FieldValue<Int>,
    val ItemTotal:FieldValue<Double>,
    val UnitPrice: FieldValue<Double>,
    val InventoryID: FieldValue<String>,
    val ProductName: FieldValue<String>

)

data class FieldValue<T>(
    val value: T
)

data class ShippingDetails(
    val city: String,
    val name: String,
    val phone: String,
    val address: String
)
