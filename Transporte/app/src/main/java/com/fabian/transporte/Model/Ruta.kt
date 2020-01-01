package com.fabian.transporte.Model

class Ruta(id:Int, nombre:String, descripcion:String, tipo:String, inicio: Marcador, fin: Marcador) {

    var id = 0
    var nombre = ""
    var descripcion = ""
    var tipo = ""
    var inicio: Marcador? = null
    var fin: Marcador? = null

    init {
        this.id = id
        this.nombre = nombre
        this.descripcion = descripcion
        this.tipo = tipo
        this.inicio = inicio
        this.fin = fin
    }

}