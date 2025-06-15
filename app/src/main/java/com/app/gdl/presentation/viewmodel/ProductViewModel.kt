package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.ProductResponse
import com.app.gdl.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<ProductResponse>()
    val products: LiveData<ProductResponse> get() = _products

    fun fetchProducts(id:String) {
        viewModelScope.launch {
            try {
                val response = repository.getproductsdata(id)
                _products.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}