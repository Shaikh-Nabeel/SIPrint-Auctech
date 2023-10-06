package com.auctech.siprint

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences


class PreferenceManager {

    companion object{

        private var preferences: SharedPreferences? = null
        private lateinit var editor: SharedPreferences.Editor

        fun init(context: Context) {
            if (preferences == null) preferences =
                context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
        }

        fun getStringValue(key: String?): String? {
            return preferences!!.getString(key, "")
        }

        fun setStringValue(key: String?, value: String?) {
            editor = preferences!!.edit()
            editor.putString(key, value)
            editor.apply()
        }


        fun getBoolValue(key: String?): Boolean {
            return preferences!!.getBoolean(key, false)
        }

        fun setBoolValue(key: String?, value: Boolean) {
            editor = preferences!!.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }

        fun getIntValue(key: String?): Int {
            return preferences!!.getInt(key, 0)
        }

        fun setIntValue(key: String?, value: Int?) {
            editor = preferences!!.edit()
            editor.putInt(key, value!!).apply()
        }

        fun deletePref() {
            val editor = preferences!!.edit()
            editor.clear()
            editor.apply()
        }
    }

}