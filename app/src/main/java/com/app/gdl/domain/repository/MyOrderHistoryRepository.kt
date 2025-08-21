package com.app.gdl.domain.repository

import com.app.gdl.data.model.MyOrdersResponse
import com.app.gdl.data.model.OrderResponse
import retrofit2.Response

interface MyOrderHistoryRepository {
    suspend fun myOderHistory(custId: Int): MyOrdersResponse

}