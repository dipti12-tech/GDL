package com.app.gdl.data.repository

import com.app.gdl.data.model.OrderRequest
import com.app.gdl.data.model.OrderResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.OrderRepository
import retrofit2.Response
import javax.inject.Inject

class OrderPlaceImp @Inject constructor(
    private val apiService: ApiService
) : OrderRepository {

    override suspend fun placeOrder(orderRequest: OrderRequest): Response<OrderResponse>  {
        return apiService.placeOrder(orderRequest)
    }
}