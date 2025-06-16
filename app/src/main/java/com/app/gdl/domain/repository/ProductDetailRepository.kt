package com.app.gdl.domain.repository

import ProductDetailsResponse

interface ProductDetailRepository {
    suspend fun getProductDetail(id: String): ProductDetailsResponse
}