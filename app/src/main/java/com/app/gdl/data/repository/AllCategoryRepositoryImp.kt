package com.app.gdl.data.repository

import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.AllCategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllCategoryRepositoryImp  @Inject constructor(
    private val apiService: ApiService
) : AllCategoryRepository {
    override suspend fun getAllCategories(): CategoryResponse {
     return apiService.getAllCategories()
    }
}