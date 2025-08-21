package com.app.gdl.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.data.model.SignupResponse
import com.app.gdl.domain.repository.SignUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignupViewModel @Inject constructor(
    private val repository: SignUpRepository
) : ViewModel() {

    private val _signupResult = MutableLiveData<Result<SignupResponse>>()
    val signupResult: LiveData<Result<SignupResponse>> = _signupResult

    fun signup(request: SignupRequest) {
        viewModelScope.launch {
            try {
                val response = repository.signup(request)
                _signupResult.value = Result.success(response)

            } catch (e: Exception) {
                _signupResult.value = Result.failure(e)
            }
        }
    }
}
