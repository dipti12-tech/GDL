package com.app.gdl.data.repository

import com.app.gdl.data.model.ProductResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.PopularItemRepository
import com.app.gdl.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

    @Singleton
    class PopularItemRepositoryImp @Inject constructor(
        private val apiService: ApiService
    ) : PopularItemRepository {
        override suspend fun getPopularItem(): ProductResponse {
            return apiService.getPopularProducts();

        }


    }