package com.fabian.transporte

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.fabian.transporte.Model.Marcador
import com.fabian.transporte.Objects.ApiCalls
import com.fabian.transporte.Utils.MapaFollow
import com.fabian.transporte.Utils.SharedPreference
import com.fabian.transporte.Utils.VolleySingleton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import org.json.JSONException
import org.json.JSONObject

class lRuta : AppCompatActivity(), OnMapReadyCallback {

    private var mapaFollow: MapaFollow? = null
    var sharedPreferences: SharedPreference? = null
    var callback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var marcadors = ArrayList<Marcador>()
    var indexRuta:Int = 0

    var LatitudTemp:Double = 0.0
    var LongitudTemp:Double = 0.0
    var destinoLat:Double? = null
    var destinoLon:Double? = null

    var MNLatitud:Double = 0.0
    var MNlongitud:Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_l_ruta)
        sharedPreferences = SharedPreference(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var toolbar = findViewById<Toolbar>(R.id.ltoolbar)
        setSupportActionBar(toolbar)
        var actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        initRequerimientoLocation()

        indexRuta = intent.getStringExtra("ID").toInt()
        datosMapa()
        //Llamada reiterada
        callback = object :LocationCallback(){
            override fun onLocationResult(location: LocationResult?) {
                super.onLocationResult(location)
                if(mapaFollow!=null){
                    mapaFollow?.configurarUbicacionBtn()
                    for(ubicacion in location?.locations!!){
                        mapaFollow?.puntoAct = LatLng(ubicacion.latitude,ubicacion.longitude)
                        mapaFollow?.miMarcador()
                        LatitudTemp = ubicacion.latitude
                        LongitudTemp = ubicacion.longitude
                    }

                    var seguimiento = sharedPreferences?.obtenerInt("seguimiento")!!
                    if(seguimiento>0){
                        actualizarPosicion(LatitudTemp,LongitudTemp,seguimiento)
                        direcciones()
                    }
                    var x = 0

                    if(marcadors.size > 0){
                        for(e in marcadors){
                            Log.d("Marcador : ",e.id.toString())
                            var distanciaMarcador = calcularDistancia(e)
                            Log.d("Distancia Marcador : ",distanciaMarcador.toString())
                            if(distanciaMarcador>=200){
                                Toast.makeText(applicationContext,"Estas Afuera de la ruta, Acercate Màs al punto de control",Toast.LENGTH_SHORT).show()
                            }else if(distanciaMarcador<=10){
                                Toast.makeText(applicationContext,"Punto de Control Alcanzado",Toast.LENGTH_SHORT).show()
                                marcadors.remove(e)
                                mapaFollow?.redrawlines(marcadors)
                            }else{
                                Toast.makeText(applicationContext,"En Ruta, Distancia Prox Punto:" + distanciaMarcador.toString(),Toast.LENGTH_SHORT).show()
                            }
                            x++
                            if(x>0){
                                break
                            }
                        }
                    }
                    var distancedestino = SphericalUtil.computeDistanceBetween(LatLng(LatitudTemp,LongitudTemp),
                        LatLng(destinoLat!!,destinoLon!!)).toInt()
                    Log.d("F/DEBUG/DistanciaD",distancedestino.toString())

                    if((distancedestino<50 && distancedestino>10) && marcadors.size == 0){
                        Toast.makeText(applicationContext,"Dirigete al Destino",Toast.LENGTH_SHORT).show()
                    }
                    if(distancedestino<=10){
                        Toast.makeText(applicationContext,"En Destino",Toast.LENGTH_SHORT).show()
                        destino()
                    }

                }
            }
        }
    }

    fun direcciones(){
        val rutaData = ListarRutas.obtenerRuta(indexRuta)
        mapaFollow?.prepararAPIGoogle(LatitudTemp,LongitudTemp,rutaData.fin?.latitud!!,rutaData.fin?.longitud!!)
    }

    fun destino(){
        var seguimiento = sharedPreferences?.obtenerInt("seguimiento")!!
        terminarseguimiento(seguimiento)
        stopUbicacion()
        finish()
    }


    fun datosMapa(){
        val rutaData = ListarRutas.obtenerRuta(indexRuta)
        var rut = sharedPreferences?.obtenerString("rut_usuario")!!
        registrarSeguimiento(LatitudTemp,LongitudTemp,rut,rutaData.id)
        destinoLat = rutaData.fin?.latitud
        destinoLon = rutaData.fin?.longitud
        marcadores(rutaData.id)

    }
    // --- Volley
    fun registrarSeguimiento(lat:Double,lon:Double,rut:String,ruta:Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_sFall,
            Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    if(!obj.getBoolean("error")){
                        Toast.makeText(applicationContext,obj.getString("message"), Toast.LENGTH_SHORT).show()
                        sharedPreferences?.guardar("seguimiento",obj.getInt("seg"))
                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(applicationContext, "Error Call : " + error?.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            })
        {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String,String> {
                val params = HashMap<String,String>()
                params.put("var","asdhwwudwnbPX")
                params.put("lat",lat.toString())
                params.put("long",lon.toString())
                params.put("rut",rut)
                params.put("ruta",ruta.toString())
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }
    fun marcadores(ruta:Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_marcadores,
            Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    if(!obj.getBoolean("error")){
                        val array = obj.getJSONArray("marcadores")
                        for (i in 0..array.length() - 1) {
                            val objectMarcador = array.getJSONObject(i)
                            val marcador = Marcador(
                                objectMarcador.getInt("marcador_id"),
                                objectMarcador.getDouble("marcador_lat"),
                                objectMarcador.getDouble("marcador_lon"),
                                ruta)
                            marcadors.add(marcador)
                        }
                        mapaFollow?.drawlines(marcadors)
                        var distancia = calcularDistancia(this.marcadors.first())//calculamos la primera distancia
                        if(distancia>150){
                            Toast.makeText(applicationContext,"Distancia Muy lejos del marcador, Terminando Ruta",Toast.LENGTH_SHORT).show()
                            finish()
                        }

                    }else{
                        Toast.makeText(applicationContext,obj.getString("message"),Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(applicationContext, "Error Call : " + error?.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            })
        {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String,String> {
                val params = HashMap<String,String>()
                params.put("var","wHWEudwnbPX")
                params.put("ruta",ruta.toString())
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }
    fun actualizarPosicion(lat:Double,lon:Double,seguimiento: Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_gps,
            Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    Toast.makeText(applicationContext,obj.getString("message"),Toast.LENGTH_SHORT).show()
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(applicationContext, "Error Call : " + error?.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            })
        {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String,String> {
                val params = HashMap<String,String>()
                params.put("var","kaasEW·woeuqP")
                params.put("lat",lat.toString())
                params.put("lon",lon.toString())
                params.put("seg",seguimiento.toString())
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }
    fun terminarseguimiento(seguimiento:Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_tFall,
            Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    Toast.makeText(applicationContext,obj.getString("message"),Toast.LENGTH_SHORT).show()
                    sharedPreferences?.removerValor("seguimiento")
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(applicationContext, "Error Call : " + error?.message, Toast.LENGTH_LONG).show()
                    finish()
                }
            })
        {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String,String> {
                val params = HashMap<String,String>()
                params.put("var","kaasEW·woeuqP")
                params.put("seguimiento",seguimiento.toString())
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }
    // --- Volley

    fun calcularDistancia(marcador:Marcador):Int{
        MNLatitud = marcador.latitud
        MNlongitud = marcador.longitud
        //120 una cuadra
        val distanceMarcador = SphericalUtil.computeDistanceBetween(LatLng(LatitudTemp,LongitudTemp),
        LatLng(MNLatitud, MNlongitud)).toInt()
        return distanceMarcador
    }

    private fun initRequerimientoLocation(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 5000
        locationRequest?.fastestInterval = 10000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapaFollow = MapaFollow(googleMap,applicationContext,resources)
        mapaFollow?.estiloMapaSeguir()
        mapaFollow?.cancelarToolsBasicos()

    }

    @SuppressLint("MissingPermission")
    private fun Ubicacion(){
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest,callback,null)
    }

    private fun stopUbicacion(){
        fusedLocationProviderClient?.removeLocationUpdates(callback)
    }

    override fun onStart() {
        super.onStart()
        Ubicacion()
    }
}
