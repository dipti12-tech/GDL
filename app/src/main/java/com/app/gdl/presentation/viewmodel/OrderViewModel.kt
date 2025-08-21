package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.OrderRequest
import com.app.gdl.data.model.OrderResponse
import com.app.gdl.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(private val repository: OrderRepository) : ViewModel() {

    private val _orderResponse = MutableLiveData<Result<OrderResponse>>()
    val orderResponse: LiveData<Result<OrderResponse>> = _orderResponse

    fun placeOrder(orderRequest: OrderRequest) {
        viewModelScope.launch {
            try {
                val response = repository.placeOrder(orderRequest)
                if (response.isSuccessful) {
                    _orderResponse.postValue(Result.success(response.body()!!))
                } else {
                    _orderResponse.postValue(Result.failure(Throwable("Error: ${response.code()}")))
                }
            } catch (e: Exception) {
                _orderResponse.postValue(Result.failure(e))
            }
        }
    }
}
