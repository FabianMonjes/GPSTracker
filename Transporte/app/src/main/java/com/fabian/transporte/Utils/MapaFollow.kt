package com.fabian.transporte.Utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fabian.transporte.Model.Marcador
import com.fabian.transporte.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson

class MapaFollow(mapa:GoogleMap, context: Context, var resources: Resources){
    private var mMap:GoogleMap? = null
    private var context:Context? = null
    private var rutaMarcada: Polyline? = null
    var listaMarcadores:ArrayList<Marker>? = null
    var puntoAct:LatLng? = null


    init {
        this.mMap = mapa
        this.context = context
    }

    fun drawlines(marcadores:ArrayList<Marcador>?){
        //recibir marcadores y dibujarlos
        var lineas = PolylineOptions()
        //FOR
        var x = 1
        if (marcadores != null) {
            for (marcador in marcadores){
                lineas.add(LatLng(marcador.latitud,marcador.longitud))
                mMap?.addMarker(MarkerOptions()
                    .position(LatLng(marcador.latitud,marcador.longitud))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.6F)
                    .title("Marcador Nº" + x.toString()))
                x++
            }
        }
        lineas.color(Color.GREEN)
            .pattern(arrayListOf<PatternItem>(Dot(), Gap(20f)))
        mMap?.addPolyline(lineas)
    }


    fun redrawlines(marcadores:ArrayList<Marcador>?){
        mMap?.clear()
        var lineas = PolylineOptions()
        var x = 1
        if (marcadores != null) {
            for (marcador in marcadores){
                lineas.add(LatLng(marcador.latitud,marcador.longitud))
                mMap?.addMarker(MarkerOptions()
                    .position(LatLng(marcador.latitud,marcador.longitud))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.6F)
                    .title("Marcador Nº" + x.toString()))
                x++
            }
        }
        lineas.color(Color.GREEN)
            .pattern(arrayListOf<PatternItem>(Dot(), Gap(20f)))
        mMap?.addPolyline(lineas)
    }


    fun prepararAPIGoogle(orlatitud:Double,orlongitud:Double,latitud:Double, longitud:Double){
        listaMarcadores = ArrayList()
        listaMarcadores?.add(mMap?.addMarker(MarkerOptions()
            .position(LatLng(latitud,longitud))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .alpha(0.6F)
            .title("Destino"))!!) // bien
        //val origen = "origin=" + punto?.latitude + "," + punto?.longitude + "&"
        val origen = "origin=" + orlatitud + "," + orlongitud + "&"
        val destino = "destination=" + latitud + "," + longitud + "&"
        val key = "key=" + resources.getString(R.string.google_maps_key)
        val parametros = origen + destino + "sensor=false&mode=driving&"+ key
        //Log.d("API","https://maps.googleapis.com/maps/api/directions/json?" + parametros)
        cargarURLAPIGooGle("https://maps.googleapis.com/maps/api/directions/json?" + parametros)
    }

    fun cargarURLAPIGooGle(url:String){
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

    fun miMarcador(){
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoAct,17.0f))
    }

    fun estiloMapaSeguir(){
        val success_style_map = mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.context, R.raw.style_map))
        if(!success_style_map!!){
            Toast.makeText(this.context, "", Toast.LENGTH_SHORT).show()
        }
    }

    fun configurarUbicacionBtn(){
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isCompassEnabled = true
    }

    fun cancelarToolsBasicos(){
        mMap?.uiSettings?.isMapToolbarEnabled = false
    }


}