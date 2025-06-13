package com.app.gdl.presentation.viewmodel

import ProductDetailsResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.domain.repository.ProductDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductDetailRepository
) : ViewModel() {

    private val _products = MutableLiveData<ProductDetailsResponse>()
    val products: LiveData<ProductDetailsResponse> get() = _products

    fun fetchProductDetail() {
        viewModelScope.launch {
            try {
                val response = repository.getProductDetail()
                _products.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}