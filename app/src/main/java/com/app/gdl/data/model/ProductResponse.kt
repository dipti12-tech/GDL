package com.app.gdl.data.model

data class ProductResponse(
    val status: Int,
    val list: List<ProductItem>,
    val s3_img_path: String
)
data class ProductItem(
    val DimensionWeight: ValueWrapper<Double>,
    val ImageUrl: ValueWrapper<String>,
    val InventoryID: ValueWrapper<String>,
    val ItemStatus: ValueWrapper<String>,
    val WeightUOM: ValueWrapper<String>,
    val CustomCategory: ValueWrapper<String>,
    val CustomDescription: ValueWrapper<String>,
    val CustomName: ValueWrapper<String>,
    val CustomSubCategory: ValueWrapper<String>
)
data class ValueWrapper<T>(
    val value: T
)
