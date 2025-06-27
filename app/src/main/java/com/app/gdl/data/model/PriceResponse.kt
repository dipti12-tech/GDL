package com.app.gdl.data.model

data class PriceResponse(
    val status: Int,
    val list: List<PriceItem>
)

data class PriceItem(
    val BreakQty: ValueDouble,
    val CustomerPriceClass: ValueString,
    val InventoryID: ValueString,
    val LastModifiedDate: ValueString,
    val Price: ValueDouble,
    val UOM: ValueString
)

data class ValueString(
    val value: String
)

data class ValueDouble(
    val value: Double
)
