package com.app.gdl.domain.repository

import com.app.gdl.data.model.OrderRequest
import com.app.gdl.data.model.OrderResponse
import retrofit2.Response

interface OrderRepository {

    suspend fun placeOrder(orderRequest: OrderRequest): Response<OrderResponse>
}
