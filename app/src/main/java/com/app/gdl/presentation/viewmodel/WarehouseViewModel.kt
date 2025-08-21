package com.app.gdl.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.gdl.data.model.MapLocation
import com.app.gdl.data.model.Warehouse
import com.app.gdl.data.model.WarehouseResponse
import com.app.gdl.domain.repository.WarehouseRepository
import com.app.gdl.utils.SharedPref
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class WarehouseViewModel @Inject constructor(
    private val repository: WarehouseRepository,
    private val sharedPref: SharedPref
) : ViewModel() {

    private val _warehouse = MutableLiveData<List<Warehouse>>()
    val warehouses: LiveData<List<Warehouse>> = _warehouse

    private val _selectedWarehouse = MutableLiveData<String>()
    val selectedWarehouse: LiveData<String> get() = _selectedWarehouse

    private val _selectedPriceClass = MutableLiveData<String>()
    val selectedPriceClass: LiveData<String> = _selectedPriceClass
    fun loadWarehouses() {
        viewModelScope.launch {
            try {
                val response = repository.getWarehouse()
                if (response.status == 1) {
                    _warehouse.postValue(response.list)
                }
            } catch (e: Exception) {
                Log.e("WarehouseVM", "Error: ${e.message}")
            }
        }
    }


    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius of the Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun onCitySelected(cityName: String, warehouseList: List<Warehouse>) {
        val selectedCity = warehouseList.find { it.city.equals(cityName, ignoreCase = true) }
        if (selectedCity == null) {
            println("City not found.")
            Log.d("City not found.", "onCitySelected: NULL")
            return
        }
/*
        if (selectedCity == null) {
            println("City not found.")
            Log.d("City not found.", "onCitySelected: NULL")
            sharedPref.default_price = "NAK CASH"
            sharedPref.near_warehouse = "NAKURU"
            _selectedPriceClass.postValue(sharedPref.default_price)
            _selectedWarehouse.postValue(
                Warehouse(
                    warehouse_name = "NAKURU",
                    warehouse_id = "NAKURU",
                    city = cityName,
                    default_price_class = "NAK CASH",
                    map_location = MapLocation(0.0.toString(), 0.0.toString())
                )
            )
            return
        }
*/

        /*   val selectedLat = selectedCity.map_location.lat.toDouble()
          val selectedLon = selectedCity.map_location.lon.toDouble()

         val nearest = warehouseList
              .filter { !it.city.equals(cityName, ignoreCase = true) } // exclude same city
              .minByOrNull { warehouse ->
                  calculateDistance(
                      selectedLat,
                      selectedLon,
                      warehouse.map_location.lat.toDouble(),
                      warehouse.map_location.lon.toDouble()
                  )
              }*/

       // if (nearest != null) {
            println("Nearest warehouse to $cityName is ${selectedCity?.warehouse_name}")
            println("Default Price Class: ${selectedCity?.default_price_class}")
            sharedPref.default_price = selectedCity?.default_price_class
            sharedPref.near_warehouse = selectedCity?.warehouse_id

            _selectedPriceClass.postValue(selectedCity?.default_price_class)
            _selectedWarehouse.postValue(selectedCity?.warehouse_id)
       /* } else {
            println("No nearby warehouse found.")
        }*/
    }
}
