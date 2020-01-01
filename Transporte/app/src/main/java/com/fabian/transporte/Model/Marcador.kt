package com.fabian.transporte.Model

class Marcador(id:Int?,latitud:Double, longitud:Double, ruta:Int?){
    var id:Int? = null
    var latitud = 0.0
    var longitud = 0.0
    var ruta:Int? = null
    init {
        this.id = id
        this.latitud = latitud
        this.longitud = longitud
        this.ruta = ruta
    }

}