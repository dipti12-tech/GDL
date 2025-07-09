package com.app.gdl.data.model

data class WarehouseResponse(
    val status: Int,
    val list: List<Warehouse>
)

data class Warehouse(
    val city: String,
    val warehouse_name: String,
    val warehouse_id: String,
    val map_location: MapLocation,
    val default_price_class: String
)

data class MapLocation(
    val lat: String,
    val lon: String
)
