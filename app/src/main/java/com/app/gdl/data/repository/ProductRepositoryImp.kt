package com.app.gdl.data.repository

import com.app.gdl.data.model.ProductResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
    class ProductRepositoryImp @Inject constructor(
        private val apiService: ApiService
    ) : ProductRepository {
        override suspend fun getproductsdata(): ProductResponse {
            return apiService.getProducts();
        }


    }