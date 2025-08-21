package com.app.gdl.domain.repository

import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.PriceResponse

interface AllCategoryIdRepository {
  suspend fun  getCategoriesIds(): CategoryResponse
}