package com.app.gdl.domain.repository

import com.app.gdl.data.model.WarehouseResponse

interface WarehouseRepository {
    suspend fun  getWarehouse(city:String): WarehouseResponse
}
