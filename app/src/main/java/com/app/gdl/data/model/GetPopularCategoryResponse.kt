package com.app.gdl.data.model

  data class GetPopularCategoryResponse(
      val status: Int,
      val category_list: List<PopularCategory>
  )

data class PopularCategory(
    val cat_id: Int,
    val category_name: String,
    val products: List<Product>
)
