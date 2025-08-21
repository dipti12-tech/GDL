package com.app.gdl.data.repository

import com.app.gdl.data.model.MyOrdersResponse
import com.app.gdl.data.model.OrderRequest
import com.app.gdl.data.model.OrderResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.MyOrderHistoryRepository
import com.app.gdl.domain.repository.OrderRepository
import retrofit2.Response
import javax.inject.Inject

class OrderHistoryImp @Inject constructor(
    private val apiService: ApiService
) : MyOrderHistoryRepository {

    override suspend fun myOderHistory(custId: Int): MyOrdersResponse {
        return apiService.getOrderHistory(custId)    }
}