package com.app.gdl.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.CategoryResponse
import com.app.gdl.domain.repository.AllCategoryIdRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllCategoryIdViewModel @Inject constructor(
    private val repository: AllCategoryIdRepository
) : ViewModel() {

    private val _allIds = MutableLiveData<CategoryResponse>()
    val allIds: LiveData<CategoryResponse> get() = _allIds

    fun fetchAllCategoriesIds() {
        viewModelScope.launch {
            try {
                val response = repository.getCategoriesIds()
                _allIds.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                    Log.e("ViewModel", "Error fetching price: ${e.message}")
                            }
        }
    }
}
