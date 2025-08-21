
data class ProductDetailsResponse(
    val status: Int,
    val details: List<Detail>,
    val s3_img_path: String

)

data class Detail(
    val BaseUOM: ValueWrapper<String>?,
val Description: ValueWrapper<String>?,
val DimensionWeight: ValueWrapper<Double>?,
val ImageUrl: ValueWrapper<String>?,
val InventoryID: ValueWrapper<String>?,
val ItemClass: ValueWrapper<String>?,
val ItemStatus: ValueWrapper<String>?,
val PostingClass: ValueWrapper<String>?,
val WeightUOM: ValueWrapper<String>?,
val CustomCategory: ValueWrapper<String>?,
val CustomDescription: ValueWrapper<String>?,
val Images: ValueWrapper<List<String>>?,
val CustomName: ValueWrapper<String>?,
val CustomSubCategory: ValueWrapper<String>?,
val DisplayItem: ValueWrapper<Int>?,
val CMSModifiedAt: ValueWrapper<String>?,
val item_price: List<ItemPrice>?
)
data class ItemPrice(
    val BreakQty: ValueWrapper<Double>?,
    val CustomerPriceClass: ValueWrapper<String>?,
    val InventoryID: ValueWrapper<String>?,
    val LastModifiedDate: ValueWrapper<String>?,
    val Price: ValueWrapper<Double>?,
    val BaseUOM: ValueWrapper<String>?,
    val UOM: ValueWrapper<String>?
)
data class ValueWrapper<T>(
    val value: T
)


