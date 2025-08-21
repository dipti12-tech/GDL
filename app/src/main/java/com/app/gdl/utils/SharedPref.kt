package com.app.gdl.utils

import android.content.SharedPreferences

/*
class SharedPref :SharedPreferences {
    override fun getAll(): MutableMap<String, *> {
        TODO("Not yet implemented")
    }

    override fun getString(key: String?, defValue: String?): String? {

    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        TODO("Not yet implemented")
    }

    override fun getInt(key: String?, defValue: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getLong(key: String?, defValue: Long): Long {
        TODO("Not yet implemented")
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        TODO("Not yet implemented")
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(key: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun edit(): SharedPreferences.Editor {
        TODO("Not yet implemented")
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("Not yet implemented")
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("Not yet implemented")
    }
}*/

import android.content.Context
import android.util.Log
import com.app.gdl.data.model.Category
import com.app.gdl.data.model.CustomerDetails
import com.app.gdl.data.model.User
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.security.MessageDigest
import javax.inject.Inject

class SharedPref @Inject constructor(private val context: Context) {

    companion object {
        private const val PREF_NAME = "UserSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ADDRESS = "userAdrress"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_MOBILE = "userMobile"
        private const val KEY_PRICE_CLASS = "priceClass"
        private const val KEY_USER_ID = "custId"
        private const val KEY_DEFAULT_PRICE = "default_price"
        private const val KEY_WAREHOUSE = "near_warehouse"
        private const val KEY_IMAGEPATH = "s3_img_path"
        private const val KEY_USER_SELECTED_ADDRESS = "selectedAddress"
        private const val KEY_CITY_ORDER = "city_Order"


    }

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = sharedPref.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userAdrress: String?
        get() = sharedPref.getString(KEY_USER_ADDRESS, null)
        set(value) = sharedPref.edit().putString(KEY_USER_ADDRESS, value).apply()

    var selectedAddress: String?
        get() = sharedPref.getString(KEY_USER_SELECTED_ADDRESS, null)
        set(value) = sharedPref.edit().putString(KEY_USER_SELECTED_ADDRESS, value).apply()

    var name: String?
        get() = sharedPref.getString(KEY_USER_NAME, null)
        set(value) = sharedPref.edit().putString(KEY_USER_NAME, value).apply()

    var mobile: String?
        get() = sharedPref.getString(KEY_USER_MOBILE, null)
        set(value) = sharedPref.edit().putString(KEY_USER_MOBILE, value).apply()

    var custid: String?
        get() = sharedPref.getString(KEY_USER_ID, null)
        set(value) = sharedPref.edit().putString(KEY_USER_ID, value).apply()

    var default_price: String?
        get() = sharedPref.getString(KEY_DEFAULT_PRICE, null)
        set(value) = sharedPref.edit().putString(KEY_DEFAULT_PRICE, value).apply()

    var near_warehouse: String?
        get() = sharedPref.getString(KEY_WAREHOUSE, null)
        set(value) = sharedPref.edit().putString(KEY_WAREHOUSE, value).apply()

    var priceclass: String?
        get() = sharedPref.getString(KEY_PRICE_CLASS, null)
        set(value) = sharedPref.edit().putString(KEY_PRICE_CLASS, value).apply()


    var cityForOrder: String?
        get() = sharedPref.getString(KEY_CITY_ORDER, null)
        set(value) = sharedPref.edit().putString(KEY_CITY_ORDER, value).apply()

    var s3_img_path: String?
        get() = sharedPref.getString(KEY_IMAGEPATH, null)
        set(value) = sharedPref.edit().putString(KEY_IMAGEPATH, value).apply()

    fun saveString(key: String, value: String) {
        sharedPref.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String = ""): String {
        return sharedPref.getString(key, default) ?: default
    }

    var citySelected: Boolean
        get() = sharedPref.getBoolean("city_selected", false)
        set(value) = sharedPref.edit().putBoolean("city_selected", value).apply()

    var hasPermissionBeenRequestedOnce: Boolean
        get() = sharedPref.getBoolean("permission_requested_once", false)
        set(value) = sharedPref.edit().putBoolean("permission_requested_once", value).apply()

    fun saveCustomerToPrefs(context: Context, customer: User) {
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(customer)
        editor.putString("customer_data", json)
        editor.apply()
    }

    fun getCustomerFromPrefs(context: Context): User? {
        val json = sharedPref.getString("customer_data", null)
        return if (json != null) {
            Gson().fromJson(json, User::class.java)
        } else null
    }

    fun saveCustomerDetailsToPrefs(context: Context, customer: CustomerDetails) {
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(customer)
        editor.putString("customer_order", json)
        editor.apply()
    }

    fun getCustomerDetailsFromPrefs(context: Context): CustomerDetails? {
        val json = sharedPref.getString("customer_order", null)
        return if (json != null) {
            Gson().fromJson(json, CustomerDetails::class.java)
        } else null
    }

    fun addInventoryId(context: Context, newId: String) {
        val ids = getInventoryIds(context).toMutableSet()
        ids.add(newId)
        sharedPref.edit().putStringSet("inventory_ids", ids).apply()
    }

    fun getInventoryIds(context: Context): Set<String> {
        return sharedPref.getStringSet("inventory_ids", emptySet()) ?: emptySet()
    }

    fun md5Hash(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    var savedCities: List<String>
        get() {
            val cityString = sharedPref.getString("savedCities", null)
            return cityString?.split(",")?.map { it.trim() } ?: emptyList()
        }
        set(value) {
            val cityString = value.joinToString(",")
            sharedPref.edit().putString("savedCities", cityString).apply()
        }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }
}
