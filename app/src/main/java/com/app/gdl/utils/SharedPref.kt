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
import com.app.gdl.data.model.User
import com.google.gson.Gson
import java.security.MessageDigest

class SharedPref(context: Context) {

    companion object {
        private const val PREF_NAME = "UserSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ADDRESS = "userAdrress"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_MOBILE = "userMobile"

    }

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = sharedPref.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = sharedPref.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userAdrress: String?
        get() = sharedPref.getString(KEY_USER_ADDRESS, null)
        set(value) = sharedPref.edit().putString(KEY_USER_ADDRESS, value).apply()

    var name: String?
        get() = sharedPref.getString(KEY_USER_NAME, null)
        set(value) = sharedPref.edit().putString(KEY_USER_NAME, value).apply()

    var mobile: String?
        get() = sharedPref.getString(KEY_USER_MOBILE, null)
        set(value) = sharedPref.edit().putString(KEY_USER_MOBILE, value).apply()

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

    fun addInventoryId(context: Context, newId: String) {
        val ids = getInventoryIds(context).toMutableSet()
        ids.add(newId)
        sharedPref.edit().putStringSet("inventory_ids", ids).apply()

        Log.d("ids ki size", "addInventoryId: "+ids.size)
        /*val idList = current?.split(",")?.mapNotNull { it.toIntOrNull() }?.toMutableList() ?: mutableListOf()

        if (!idList.contains(newId.toInt())) {
            idList.add(newId.toInt())
            val updated = idList.joinToString(",")
            sharedPref.edit().putString("inventory_ids", updated).apply()
        }*/
    }
    /*fun getInventoryIds(context: Context): List<Int> {
        val stored = sharedPref.getString("inventory_ids", "") ?: ""
        return stored.split(",").mapNotNull { it.toIntOrNull() }
    }
*/

    fun getInventoryIds(context: Context): Set<String> {
        return sharedPref.getStringSet("inventory_ids", emptySet()) ?: emptySet()
    }

    fun md5Hash(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun clearSession() {
        sharedPref.edit().clear().apply()
    }
}
