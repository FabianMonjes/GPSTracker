package com.fabian.transporte

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

import androidx.core.app.ActivityCompat
import com.fabian.transporte.Utils.MapaInicio
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class Inicio : AppCompatActivity(), OnMapReadyCallback {

    private var mapaInicio: MapaInicio? = null
    //permisos
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val codigo_solicitud_permiso = 600
    //Ubicacion
    var callback: LocationCallback? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var toolbar = findViewById<Toolbar>(R.id.ltoolbar)
        setSupportActionBar(toolbar)

        fusedLocationProviderClient = FusedLocationProviderClient(this)
        initLocationRequest()
        callback = object: LocationCallback(){
            override fun onLocationResult(location: LocationResult?) {
                super.onLocationResult(location)
                if(mapaInicio != null){
                    mapaInicio?.habilitarMiUbicacion()
                    for(ubicacion in location?.locations!!){

                        mapaInicio?.punto = LatLng(ubicacion.latitude, ubicacion.longitude)//punto de latitud y longitud! (ubicacion)
                        mapaInicio?.miMarcador()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inicio,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.mFollow->{
                var intent  = Intent(this,ListarRutas::class.java)
                startActivity(intent)
                return true
            }
            R.id.mProfile->{
                var intent = Intent(this,Perfil::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    private fun initLocationRequest(){
        locationRequest = LocationRequest() //ubicacion tiempo real
        locationRequest?.interval = 10000//cuanto tiempo se actualizara
        locationRequest?.fastestInterval = 50000 // velocidad Maxima que se va a demorar de interval
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY // calculo de proximidad de acuerdo a la ubicacion.
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mapaInicio =
            MapaInicio(googleMap, applicationContext, resources)
        mapaInicio?.estiloMapa()
        // Add a marker in Sydney and move the camera

    }
    private fun validarPermisoUbicacion():Boolean{
        //revisa si hay permisos otorgados...
        //checkea permisos si previamente se han otorgado devuelve true o false.
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this,permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this,permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED
        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }
    @SuppressLint("MissingPermission")
    private fun miUbicacion(){
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest,callback,null)
    }
    private fun solicitarPermisos(){
        //mandar mensaje de explicacion
        val ContextoProveer = ActivityCompat.shouldShowRequestPermissionRationale(this,permisoFineLocation)
        if(ContextoProveer){
            sPermiso()
        }else{
            sPermiso()
        }
    }
    private fun sPermiso(){
        //habilita una ventana para pedir permisos
        requestPermissions(arrayOf(permisoFineLocation,permisoCoarseLocation),codigo_solicitud_permiso)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            codigo_solicitud_permiso->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    miUbicacion() //se dio permiso, obtener ubicacion
                }else{
                    Toast.makeText(this,"No diste Permiso para calcular ubicacion", Toast.LENGTH_SHORT).show() //no se dio permiso
                }
            }
        }
    }
    private fun stopCheckMiUbicacion(){
        //cuando se pausa , se detenga, (no es importante en segundo plano)
        fusedLocationProviderClient?.removeLocationUpdates(callback)
    }
    override fun onStart() {
        // a empear
        super.onStart()
        if(validarPermisoUbicacion()){
            miUbicacion()
        }else{
            solicitarPermisos()
        }
    }
    override fun onPause() {
        super.onPause()
        //detener la actualizacion de ubicacion
        stopCheckMiUbicacion()
    }
}
