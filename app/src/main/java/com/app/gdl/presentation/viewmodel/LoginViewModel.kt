package com.app.gdl.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.LoginRequest
import com.app.gdl.data.model.LoginResponse
import com.app.gdl.domain.model.CustomerEntity
import com.app.gdl.domain.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: LoginRepository) : ViewModel() {

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    fun loginUser(request: LoginRequest) {
        Log.d("requestlogin", "loginUser: "+request)
        viewModelScope.launch {
            val response = repository.loginUser(request)
            Log.d("LoginViewModel", "loginUser: $response")
            if (response.isSuccessful) {
                response.body()?.let {
                    _loginResponse.postValue(it)
                    it.customer_details.let { details ->
                        val customer = details?.let { it1 ->
                            CustomerEntity(
                                it1.customer_id,
                                details.first_name,
                                details.last_name,
                                details.email_id,
                                details.phone,
                                details.address
                            )
                        }
                        if (customer != null) {
                            repository.saveCustomerLocally(customer)
                        }
                        Log.d("LoginViewModel**", "loginUser: " + repository.getCustomerLocally())
                    }
                }
            }
        }
    }
}
