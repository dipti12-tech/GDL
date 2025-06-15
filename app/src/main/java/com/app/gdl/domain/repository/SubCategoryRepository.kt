package com.app.gdl.domain.repository

import com.app.gdl.data.model.CategoryResponse

    interface SubCategoryRepository {
        suspend fun  getSubCategory(id:String): CategoryResponse
    }
