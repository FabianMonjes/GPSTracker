package com.fabian.transporte.Utils

import android.app.Application
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton : Application() {

    companion object {
        private val TAG = VolleySingleton::class.java.simpleName
        @get:Synchronized var instancia: VolleySingleton? = null
            private set
    }

    val requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                return Volley.newRequestQueue(applicationContext)
            }
            return field
        }

    override fun onCreate() {
        super.onCreate()
        instancia = this
    }

    fun <T> addSolicitudVolley(request: Request<T>) {
        request.tag = TAG
        requestQueue?.add(request)
    }

}