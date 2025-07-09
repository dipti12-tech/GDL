package com.app.gdl.domain.repository

import com.app.gdl.data.model.CategoryResponse

interface CategoryRepository {
    suspend fun getCategories(): CategoryResponse
}

