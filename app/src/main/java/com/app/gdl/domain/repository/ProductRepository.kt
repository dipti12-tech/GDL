package com.app.gdl.domain.repository

import com.app.gdl.data.model.ProductResponse

interface ProductRepository {
    suspend fun  getproductsdata(id:String,priceclass:String): ProductResponse
}