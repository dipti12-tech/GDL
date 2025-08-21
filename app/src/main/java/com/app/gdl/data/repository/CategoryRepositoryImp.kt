package com.app.gdl.data.repository
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.CategoryRepository
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.ProductListResponse
import javax.inject.Inject
import javax.inject.Singleton

    @Singleton
    class CategoryRepositoryImp @Inject constructor(
        private val apiService: ApiService
    ) : CategoryRepository {

        override suspend fun getCustomList(priceClass:String,warehouse:String): ProductListResponse {
           return apiService.getCustomLists(priceClass,warehouse)
        }

    }
