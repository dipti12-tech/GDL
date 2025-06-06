package com.app.gdl.data.repository

import android.util.Log
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.data.model.SignupResponse
import com.app.gdl.data.remote.ApiService
import com.app.gdl.domain.repository.SignUpRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SignUpRepository {

    override suspend fun signup(request: SignupRequest): SignupResponse {
        val response = apiService.signUp(request)
        Log.d("SignupRepositoryImpl", "signup: RESPONSE" + response)
        /*   if (response.message) {
            return response.body()!!
        } else {
            throw Exception("Signup failed: ${response.message()}")
        }*/
        return response
}
}
