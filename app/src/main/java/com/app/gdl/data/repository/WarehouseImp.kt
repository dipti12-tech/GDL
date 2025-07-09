package com.app.gdl.data.repository

import com.app.gdl.data.model.PriceResponse
import com.app.gdl.data.model.WarehouseResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.DefaultPriceRepository
import com.app.gdl.domain.repository.WarehouseRepository
import javax.inject.Inject

class WarehouseImp @Inject constructor(
    private val apiService: ApiService
) : WarehouseRepository {

    override suspend fun getWarehouse(city: String): WarehouseResponse {
        return apiService.getWarehouses(city)

    }
}
