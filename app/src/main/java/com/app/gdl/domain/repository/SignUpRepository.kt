package com.app.gdl.domain.repository

import com.app.gdl.data.model.SignupRequest
import com.app.gdl.data.model.SignupResponse

interface SignUpRepository {
    suspend fun signup(request: SignupRequest):SignupResponse
}