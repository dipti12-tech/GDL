package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.domain.repository.CategoryRepository
import com.app.gdl.domain.repository.SubCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubCategoryViewModel @Inject constructor(
    private val repository: SubCategoryRepository
) : ViewModel() {

    private val _categories = MutableLiveData<CategoryResponse>()
    val categories: LiveData<CategoryResponse> get() = _categories

    fun fetchSubCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getSubCategory()
                _categories.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}