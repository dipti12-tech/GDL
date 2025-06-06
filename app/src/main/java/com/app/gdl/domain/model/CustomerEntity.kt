package com.app.gdl.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.gdl.data.model.AddressDetails
import com.app.gdl.utils.AppData

@Entity(tableName = AppData.tableName)
data class CustomerEntity(
    @PrimaryKey val customerId: Int,
    val firstName: String?,
    val lastName: String?,
    val emailId: String?,
    val phone: String?,
    val address: List<AddressDetails>?

)
