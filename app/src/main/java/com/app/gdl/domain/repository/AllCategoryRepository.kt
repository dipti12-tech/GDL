package com.app.gdl.domain.repository

import com.app.gdl.data.model.CategoryResponse

interface AllCategoryRepository {

    suspend fun getFeaturedCategories(): CategoryResponse

}