package com.app.gdl.data.repository
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.CategoryRepository
import com.app.gdl.data.model.CategoryResponse
import javax.inject.Inject
import javax.inject.Singleton

    @Singleton
    class CategoryRepositoryImp @Inject constructor(
        private val apiService: ApiService
    ) : CategoryRepository {
        override suspend fun getCategories(): CategoryResponse {
            return apiService.getCategories()
        }

    }
