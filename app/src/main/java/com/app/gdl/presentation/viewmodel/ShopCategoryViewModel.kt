package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.domain.repository.AllCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopCategoryViewModel @Inject constructor(
    private val repository: AllCategoryRepository
) : ViewModel() {

    private val _allcategories = MutableLiveData<CategoryResponse>()
    val allcategories: LiveData<CategoryResponse> get() = _allcategories

    fun fetchFeaturedCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getFeaturedCategories()
                _allcategories.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}