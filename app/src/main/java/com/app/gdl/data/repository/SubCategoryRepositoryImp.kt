package com.app.gdl.data.repository

import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.SubCategoryRepository
import javax.inject.Inject

class SubCategoryRepositoryImp  @Inject constructor(
    private val apiService: ApiService
) : SubCategoryRepository {

    override suspend fun getSubCategory(): CategoryResponse {
        return apiService.getSubCategory();
    }
}

