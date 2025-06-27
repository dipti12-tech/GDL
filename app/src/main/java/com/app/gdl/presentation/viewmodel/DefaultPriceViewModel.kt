package com.app.gdl.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.PriceResponse
import com.app.gdl.domain.repository.DefaultPriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultPriceViewModel @Inject constructor(
    private val repository: DefaultPriceRepository
) : ViewModel() {

    private val _defaultPrice = MutableLiveData<PriceResponse>()
    val defaultPrice: LiveData<PriceResponse> get() = _defaultPrice

    fun fetchDefaultPrice(priceclass:String) {
        viewModelScope.launch {
            try {
                val response = repository.getDefautPrice(priceclass)
                _defaultPrice.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}