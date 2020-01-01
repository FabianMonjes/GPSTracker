package com.fabian.transporte.Model

class Conductor (rut:String, nombre:String,apellido:String, telefono:String, correo:String ){
    var rut  = ""
    var nombre = ""
    var apellido  = ""
    var telefono = ""
    var correo = ""

    init {
        this.rut = rut
        this.nombre = nombre
        this.apellido = apellido
        this.telefono = telefono
        this.correo = correo
    }
}