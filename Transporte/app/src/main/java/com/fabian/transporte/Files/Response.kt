package com.fabian.transporte.Files

import com.google.android.gms.maps.model.LatLng

//clase respuesta con un arreglo de Ruta
class Response{
    var routes:ArrayList<Routes>? = null // Rutas
}

// Ruta es un arreglo de tipo paso
class Routes{
    var legs:ArrayList<Legs>? = null //PASOS
}

class Legs{
    var steps:ArrayList<Steps>? = null
}

//
class Steps{
    var end_location: LatLon? = null
    var start_location: LatLon? = null
}

class LatLon{
    var lat:Double = 0.0
    var lng:Double = 0.0

    fun toLatLng():LatLng{
        return LatLng(lat, lng)
    }
}

class Polyline{
    var points:String = ""
}