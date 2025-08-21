package com.app.gdl.data.model

import android.os.Parcelable
import com.app.gdl.utils.SlotType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductListResponse(
    val status: Int,
    val list: List<ProductListItem>,
    val s3_img_path: String

):Parcelable

@Parcelize
data class ProductListItem(
    val list_id: Int,
    val list_name: String,
    val list_desc: String,
    val list_img_path:String,
    val slot: String,
    val status: Int,
    val products: List<Product>,
    val created_at: String,
    val updated_at: String?
)
    :Parcelable{
    val slotType: SlotType
        get() = SlotType.fromCode(slot)
}

@Parcelize
data class Product(
    val BaseUOM:ValueDataString?,
    val DimensionWeight: ValueDataDouble?,
    val ImageUrl: ValueDataString?,
    val InventoryID: ValueDataString?,
    val ItemStatus: ValueDataString?,
    val WeightUOM: ValueDataString?,
    val CustomCategory: ValueDataString?,
    val CustomDescription: ValueDataString?,
    val CustomName: ValueDataString?,
    val CustomSubCategory: ValueDataString?,
    val order: ValueDataString?,
    val item_price: ValueDataDouble?
):Parcelable

@Parcelize
data class ValueDataString(
    val value: String?
) : Parcelable

@Parcelize
data class ValueDataDouble(
    val value: Double?
) : Parcelable

