package com.app.gdl.data.repository

import com.app.gdl.data.model.PriceResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.DefaultPriceRepository
import javax.inject.Inject

class DefaultPricesImp @Inject constructor(
    private val apiService: ApiService
) : DefaultPriceRepository {

    override suspend fun getDefautPrice(priceclass:String): PriceResponse {
        return apiService.getDefaultPrices(priceclass)
    }
}