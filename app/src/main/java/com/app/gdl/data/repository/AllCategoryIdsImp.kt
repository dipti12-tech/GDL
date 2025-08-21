package com.app.gdl.data.repository

import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.AllCategoryIdRepository
import javax.inject.Inject

class AllCategoryIdsImp @Inject constructor(
    private val apiService: ApiService
) : AllCategoryIdRepository {

    override suspend fun getCategoriesIds(): CategoryResponse {
        return apiService.getAllCategories()
    }
}
