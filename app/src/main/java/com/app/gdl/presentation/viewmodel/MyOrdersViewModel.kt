package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.gdl.data.model.MyOrdersResponse
import com.app.gdl.domain.repository.MyOrderHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyOrdersViewModel @Inject constructor(private val repository: MyOrderHistoryRepository) :
    ViewModel() {
    private val _orders = MutableLiveData<MyOrdersResponse>()
    val orders: LiveData<MyOrdersResponse> = _orders

    suspend fun fetchOrders(custId: Int) {
        val response: MyOrdersResponse = repository.myOderHistory(custId)
        _orders.value = response
    }
}