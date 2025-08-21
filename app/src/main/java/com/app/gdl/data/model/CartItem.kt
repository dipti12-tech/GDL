package com.app.gdl.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val inventoryId: String,
    val name: String,
    val imageUrl: String,
    val unit: String,
    val BaseUOM: String,
    var quantity: Int,
    val pricePerUnit: Double,
    val category: String
) : Parcelable
