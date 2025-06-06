package com.app.gdl.domain.repository

import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.GetPopularCategoryResponse

interface GetPopularCategoryRepository {
    suspend fun  getpopularCategory(): GetPopularCategoryResponse
}