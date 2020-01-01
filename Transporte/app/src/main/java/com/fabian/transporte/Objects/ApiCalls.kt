package com.fabian.transporte.Objects

object ApiCalls {
    private val url_root = "http://www.paratodo.cl/mapa/v1/?f="
    val call_login = url_root + "login"
    val call_routes = url_root + "rutas"
    val call_logs = url_root + "logsp"
    val call_clogin = url_root + "clogin"
    val call_sFall = url_root + "seguir"
    val call_uFall = url_root +"aseguirpot"
    val call_tFall = url_root +"tseguir"
    val call_tbFall = url_root +"tbseguir"
    val call_marcadores = url_root + "marcadores"
    val call_gps = url_root + "gps"
}