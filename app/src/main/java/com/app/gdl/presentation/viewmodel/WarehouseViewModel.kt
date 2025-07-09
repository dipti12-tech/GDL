package com.app.gdl.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.Warehouse
import com.app.gdl.data.model.WarehouseResponse
import com.app.gdl.domain.repository.WarehouseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class WarehouseViewModel @Inject constructor(
    private val repository:  WarehouseRepository
) : ViewModel() {


  private val _warehouse = MutableLiveData<List<Warehouse>>()
    val warehouses: LiveData<List<Warehouse>> = _warehouse

    private val _selectedPriceClass = MutableLiveData<String>()
    val selectedPriceClass: LiveData<String> = _selectedPriceClass
    fun loadWarehouses() {
        viewModelScope.launch {
            try {
                val response = repository.getWarehouse("ELDORET")
                if (response.status == 1) {
                    _warehouse.postValue(response.list)
                }
            } catch (e: Exception) {
                Log.e("WarehouseVM", "Error: ${e.message}")
            }
        }
    }

    fun onCitySelected(city: String) {
        val selected = _warehouse.value?.find { it.city.equals(city, ignoreCase = true) }
        selected?.let {
            val selectedLat = it.map_location.lat.toDouble()
            val selectedLon = it.map_location.lon.toDouble()

            val nearest = _warehouse.value
                ?.minByOrNull { warehouse ->
                    calculateDistance(
                        selectedLat, selectedLon,
                        warehouse.map_location.lat.toDouble(),
                        warehouse.map_location.lon.toDouble()
                    )
                }

            nearest?.let { near ->
                _selectedPriceClass.postValue(near.default_price_class)
            }
        }
    }
        private fun calculateDistance(
            lat1: Double, lon1: Double, lat2: Double, lon2: Double
        ): Double {
            val r = 6371 // km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }
