package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.GetPopularCategoryResponse
import com.app.gdl.domain.repository.GetPopularCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MostFamousCategoryViewModel @Inject constructor(
    private val repository: GetPopularCategoryRepository

) : ViewModel() {
    private val _categories = MutableLiveData<GetPopularCategoryResponse>()
    val getpopularcategories: LiveData<GetPopularCategoryResponse> get() = _categories

    fun fetchGetpopularCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getpopularCategory()
                _categories.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
