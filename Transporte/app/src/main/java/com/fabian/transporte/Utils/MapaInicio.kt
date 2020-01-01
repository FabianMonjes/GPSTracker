package com.fabian.transporte.Utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fabian.transporte.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polyline
import com.google.gson.Gson

class MapaInicio(mapa: GoogleMap, context: Context, var resource: Resources) {

    private var mMap:GoogleMap? = null
    private var context:Context? = null
    var punto: LatLng? = null
    private var rutaMarcada: Polyline? = null
    private var marcadores:ArrayList<Marker>? = null

    init {
        this.mMap = mapa
        this.context = context
    }

    fun estiloMapa(){

        //Estilo de MAPA
        val success_style_map = mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.context,
            R.raw.style_map
        ))
        if(!success_style_map!!){
            Toast.makeText(this.context,"Error al cargar estilo del mapa",Toast.LENGTH_SHORT).show()
            //Error de cambiar el tipo de mapa
        }
    }

    fun habilitarMiUbicacion(){
        //Habilita el boton de ubicacion
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
    }

    fun cancelarTools(){
        mMap?.uiSettings?.isMapToolbarEnabled = false
    }

    fun miMarcador(){
        //coloca un marcador en donde me encuentro actualmente
        //mMap?.addMarker(MarkerOptions().position(punto!!).title("Estoy Aqui!")) //marcador del punto con un titulo
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(punto,16.0f))
    }

    fun prepararAPI(orlatitud:Double,orlongitud:Double,latitud:Double, longitud:Double){
        marcadores = ArrayList()
        marcadores?.add(mMap?.addMarker(MarkerOptions()
            .position(LatLng(latitud,longitud))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .alpha(0.6F)
            .title("Destino"))!!) // bien
        //val origen = "origin=" + punto?.latitude + "," + punto?.longitude + "&"
        val origen = "origin=" + orlatitud + "," + orlongitud + "&"
        val destino = "destination=" + latitud + "," + longitud + "&"
        val key = "key=" + resource.getString(R.string.google_maps_key)
        val parametros = origen + destino + "sensor=false&mode=driving&"+ key
        //Log.d("API","https://maps.googleapis.com/maps/api/directions/json?" + parametros)
        cargarURLAPI("https://maps.googleapis.com/maps/api/directions/json?" + parametros)
    }

    fun cargarURLAPI(url:String){
        val queue = Volley.newRequestQueue(this.context)
        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String>{
                response ->
            //Log.d("HTTP",response)
            val coordenadas = obtenerCoordenadasJSON(response)
            drawingRoute(coordenadas)
        }, Response.ErrorListener {
            Toast.makeText(this.context,"Error en el la comunicacion con la API GOOGLE",Toast.LENGTH_SHORT).show()
        })
        queue.add(solicitud)
    }

    fun drawingRoute(coordenadas:PolylineOptions){
        if(rutaMarcada!=null){
            rutaMarcada?.remove()
        }
        //Toast.makeText(this.context,"Dibujando",Toast.LENGTH_SHORT).show()
        rutaMarcada = mMap?.addPolyline(coordenadas)
    }

    fun obtenerCoordenadasJSON(json:String):PolylineOptions{
        val gson = Gson()
        val objeto = gson.fromJson(json, com.fabian.transporte.Files.Response::class.java)
        val puntos = objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!
        val coordenadas = PolylineOptions()
        for (punto in puntos){
            coordenadas.add(punto.start_location?.toLatLng())
            coordenadas.add(punto.end_location?.toLatLng())
        }
        coordenadas
            .color(Color.WHITE)
            .width(8f)
        return coordenadas
    }
}