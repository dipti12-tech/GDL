package com.app.gdl.data.model

    data class GetPopularCategoryResponse(
        val status: Int,
        val category_list: List<Category>,
        val message: String
    )

    data class PopularCategory(
        val cat_id: Int,
        val category_name: String,
        val category_desc: String,
        val category_img_path: String,
        val parent_cat_id: Int,
        val status: Int,
        val created_at: String
    )
