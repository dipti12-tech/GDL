package com.app.gdl.data.repository

import ProductDetailsResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.ProductDetailRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductDetailsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductDetailRepository {

    override suspend fun getProductDetail(id: String): ProductDetailsResponse {
        return apiService.getProductsDetails(id)
    }
}