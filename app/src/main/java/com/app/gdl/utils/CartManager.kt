package com.app.gdl.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.app.gdl.data.model.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {

    private const val PREF_NAME = "cart_prefs"
    private const val KEY_CART_ITEMS = "cart_items"

    private val cartItems = mutableListOf<CartItem>()
    private lateinit var preferences: SharedPreferences
    private lateinit var pref: SharedPref

    private val gson = Gson()

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadCart()
    }

    fun addItem(item: CartItem) {
        val index =
            cartItems.indexOfFirst { it.inventoryId == item.inventoryId && it.unit == item.unit }
        if (index != -1) {
            val existing = cartItems[index]
            cartItems[index] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            cartItems.add(item)
        }
        saveCart()
    }

    fun getItems(): List<CartItem> = cartItems

    fun removeItem(productId: String, unit: String) {
        cartItems.removeAll { it.inventoryId == productId && it.unit == unit }
        saveCart()
    }
    /* fun removeItem(inventoryId: String, unit: String) {
         cartItems.removeIf { it.inventoryId == inventoryId && it.unit == unit }
     }*/


    fun clearCart() {
        cartItems.clear()
        saveCart()
    }

    private fun saveCart() {
        val json = gson.toJson(cartItems)
        preferences.edit().putString(KEY_CART_ITEMS, json).apply()
        Log.d("saveCart", "onViewCreated: " + CartManager.getItems())

    }

    private fun loadCart() {
        val json = preferences.getString(KEY_CART_ITEMS, null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<CartItem>>() {}.type
            val items: MutableList<CartItem> = gson.fromJson(json, type)
            cartItems.clear()
            cartItems.addAll(items)
        }
    }
}
