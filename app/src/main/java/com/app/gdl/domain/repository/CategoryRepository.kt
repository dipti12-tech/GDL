package com.app.gdl.domain.repository

import com.app.gdl.data.model.ProductListResponse

interface CategoryRepository {
    //suspend fun getCategories(): CategoryResponse
    suspend fun getCustomList(priceClass:String,warehouse: String): ProductListResponse

}

