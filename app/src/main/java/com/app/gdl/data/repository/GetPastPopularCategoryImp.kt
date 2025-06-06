package com.app.gdl.data.repository

import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.GetPopularCategoryResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.GetPopularCategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPastPopularCategoryImp @Inject constructor(
    private val apiService: ApiService
) : GetPopularCategoryRepository {

    override suspend fun getpopularCategory(): GetPopularCategoryResponse {
        return apiService.getPopularCategories()
    }
}