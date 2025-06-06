package com.app.gdl.domain.repository

import com.app.gdl.data.local.AppDatabase
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.data.model.LoginResponse
import com.app.gdl.domain.model.CustomerEntity
import retrofit2.Response
import android.content.Context
import com.app.gdl.GilanisApplication

interface LoginRepository {

    suspend fun loginUser(request: LoginRequest): Response<LoginResponse>
    suspend fun saveCustomerLocally(customer: CustomerEntity)
    suspend fun getCustomerLocally(): CustomerEntity?
}
