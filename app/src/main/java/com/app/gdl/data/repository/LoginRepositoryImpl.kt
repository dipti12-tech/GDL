package com.app.gdl.data.repository

import android.content.Context
import com.app.gdl.data.local.AppDatabase
import com.app.gdl.data.local.CustomerDao
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.data.model.LoginResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.model.CustomerEntity
import com.app.gdl.domain.repository.LoginRepository
import retrofit2.Response


class LoginRepositoryImpl(
    private val context: Context,
    private val apiService: ApiService
    ) : LoginRepository {

    private val customerDao = AppDatabase.getInstance(context).userDao()

    override suspend fun loginUser(request: LoginRequest): Response<LoginResponse> {
            return apiService.loginUser(request)
        }

        override suspend fun saveCustomerLocally(customer: CustomerEntity) {
            customerDao.insertCustomer(customer)
        }

        override suspend fun getCustomerLocally(): CustomerEntity {
            return customerDao.getCustomer()
        }
    }
