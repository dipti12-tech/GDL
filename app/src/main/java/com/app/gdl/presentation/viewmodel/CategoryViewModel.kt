package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.data.model.ProductListResponse
import com.app.gdl.data.model.Warehouse
import com.app.gdl.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableLiveData<ProductListResponse>()
    val categories: LiveData<ProductListResponse> get() = _categories

    fun fetchCustomList(priceClass:String,warehouse: String) {
        viewModelScope.launch {
            try {
                val response = repository.getCustomList(priceClass,warehouse)
                _categories.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}