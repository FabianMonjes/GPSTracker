package com.fabian.transporte

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.fabian.transporte.Objects.ApiCalls
import com.fabian.transporte.Utils.MapaInicio
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


class sRuta : AppCompatActivity(), OnMapReadyCallback {


    private var mapaInicio: MapaInicio? = null
    var sharedPreferences: SharedPreference? = null
    var callback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null

    var indexRuta:Int = 0

    //fix

    var LatitudTemp:Double = 0.0
    var LongitudTemp:Double = 0.0
    var destinoLat:Double? = null
    var destinoLon:Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_s_ruta)
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
        initLocationRequest()

        indexRuta = intent.getStringExtra("ID").toInt()
        Toast.makeText(applicationContext,"Iniciar Ruta",Toast.LENGTH_SHORT).show()
        callback = object: LocationCallback(){
            override fun onLocationResult(location: LocationResult?) {
                super.onLocationResult(location)
                if(mapaInicio != null){
                    mapaInicio?.habilitarMiUbicacion()
                    for(ubicacion in location?.locations!!){
                        mapaInicio?.punto = LatLng(ubicacion.latitude, ubicacion.longitude)//punto de latitud y longitud! (ubicacion)
                        mapaInicio?.miMarcador()

                        LatitudTemp = ubicacion.latitude
                        LongitudTemp = ubicacion.longitude

                    }
                    mapData()
                    var seguimiento = sharedPreferences?.obtenerInt("seguimiento")!!
                    if(seguimiento>0){
                        actualizarSeguimientoPot(LatitudTemp,LongitudTemp,seguimiento)
                    }
                }
                var distance = SphericalUtil.computeDistanceBetween(LatLng(LatitudTemp,LongitudTemp),
                    LatLng(destinoLat!!,destinoLon!!)).toInt()

                if(distance<=10){
                    destinollegado()
                }


            }
        }

    }

    private fun initLocationRequest(){
        locationRequest = LocationRequest() //ubicacion tiempo real
        locationRequest?.interval = 5000//cuanto tiempo se actualizara
        locationRequest?.fastestInterval = 10000 // velocidad Maxima que se va a demorar de interval
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY // calculo de proximidad de acuerdo a la ubicacion.
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapaInicio =
            MapaInicio(googleMap, applicationContext, resources)
        mapaInicio?.estiloMapa()
        mapaInicio?.cancelarTools()
    }

    fun mapData(){
        val rutaData = ListarRutas.obtenerRuta(indexRuta)
        //var tipoRuta = rutaData.tipo
        var rut = sharedPreferences?.obtenerString("rut_usuario")!!
        registrarSeguimiento(LatitudTemp,LongitudTemp,rut,rutaData.id)
        destinoLat = rutaData.fin?.latitud
        destinoLon = rutaData.fin?.longitud
        if (destinoLat != null && destinoLon !=null) {
            mapaInicio?.prepararAPI(LatitudTemp,LongitudTemp,destinoLat!!,destinoLon!!)
        }
    }

    fun destinollegado(){
        var seguimiento = sharedPreferences?.obtenerInt("seguimiento")!!
        terminarseguimiento(seguimiento)
        stopCheckMiUbicacion()
        finish()
    }

    fun registrarSeguimiento(lat:Double,lon:Double,rut:String,ruta:Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_sFall,
            Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    if(!obj.getBoolean("error")){
                        Toast.makeText(applicationContext,obj.getString("message"),Toast.LENGTH_SHORT).show()
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

    fun actualizarSeguimientoPot(lat:Double,lon:Double,seguimiento:Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_uFall,
            Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    if(!obj.getBoolean("error")){
                        Toast.makeText(applicationContext,obj.getString("message"),Toast.LENGTH_SHORT).show()
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
                params.put("var","kasgfawoeuqP")
                params.put("lat",lat.toString())
                params.put("long",lon.toString())
                params.put("seguimiento",seguimiento.toString())
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sruta,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home->{
                finish()
                return true
            }
            else->{return super.onOptionsItemSelected(item)}
        }
    }
    @SuppressLint("MissingPermission")
    private fun miUbicacion(){
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest,callback,null)
    }

    private fun stopCheckMiUbicacion(){
        //cuando se pausa , se detenga, (no es importante en segundo plano)
        fusedLocationProviderClient?.removeLocationUpdates(callback)
    }

    override fun onStart() {
        // a empear
        super.onStart()
        miUbicacion()
    }
}
