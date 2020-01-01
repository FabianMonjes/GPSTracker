package com.fabian.transporte.Utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(val context:Context) {
    private val PREFS_NAME = "preferences_scp"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE)

    fun guardar(key:String,texto:String){
        val editor:SharedPreferences.Editor = sharedPref.edit()
        editor.putString(key,texto)
        editor.apply()
    }

    fun guardar(key:String,valor:Int){
        val editor:SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(key,valor)
        editor.apply()
    }

    fun guardar(key:String,estado:Boolean){
        val editor:SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(key, estado)
        editor.apply()
    }

    fun obtenerString(key:String):String?{
        return sharedPref.getString(key,null)
    }

    fun obtenerInt(key:String):Int{
        return sharedPref.getInt(key,0)
    }

    fun obtenerEstado(key:String,valorDefecto:Boolean):Boolean{
        return sharedPref.getBoolean(key,valorDefecto)
    }

    fun limpiarPreferencias(){
        val editor:SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removerValor(key:String){
        val editor:SharedPreferences.Editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()
    }
}