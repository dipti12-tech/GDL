package com.app.gdl.utils

import androidx.room.TypeConverter
import com.app.gdl.data.model.AddressDetails
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromAddressList(value: List<AddressDetails>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAddressList(value: String): List<AddressDetails>? {
        val listType = object : TypeToken<List<AddressDetails>>() {}.type
        return Gson().fromJson(value, listType)
    }
}