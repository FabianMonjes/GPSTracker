package com.fabian.transporte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.fabian.transporte.Objects.ApiCalls
import com.fabian.transporte.Utils.SharedPreference
import com.fabian.transporte.Utils.VolleySingleton
import org.json.JSONException
import org.json.JSONObject

class Perfil : AppCompatActivity() {

    var sharedPreferences: SharedPreference? = null
    var logs:ArrayList<String> = ArrayList()
    var adaptador:ArrayAdapter<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        sharedPreferences = SharedPreference(this)

        var nombretx = findViewById<TextView>(R.id.txtPerfilNombre)
        var apellidotx = findViewById<TextView>(R.id.txtPerfilApellido)
        var lastRecordtx = findViewById<TextView>(R.id.txtPerfilFechaIn)

        //nombre,inicio,termino?,ficha de apertura y cierre del sistema hace 30 dias atras.
        nombretx.text = sharedPreferences?.obtenerString("nombre_usuario")
        apellidotx.text = sharedPreferences?.obtenerString("apellido_usuario")
        lastRecordtx.text = sharedPreferences?.obtenerString("fecha_ingreso")

        //lista
        logs.clear()
         //
        cargarlogs(sharedPreferences?.obtenerString("rut_usuario")!!)

        val listaLogs = findViewById<ListView>(R.id.lista_logs)

        adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, logs)

        listaLogs.adapter = adaptador


        var toolbar = findViewById<Toolbar>(R.id.ltoolbar)
        setSupportActionBar(toolbar)

        var actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

    }

    fun cargarlogs(rut:String){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_logs,
            Response.Listener<String>{
                    response ->
                try {
                    val obj = JSONObject(response)
                    if (!obj.getBoolean("error")) {
                        val array = obj.getJSONArray("logs")
                        for (i in 0..array.length() - 1) {
                            val objectLog = array.getJSONObject(i)
                            var terminoLog = objectLog.getString("registrolog_termino")
                            if(terminoLog == "null"){
                                terminoLog = "Sesion Abierta"
                            }
                            logs.add("Log: "+objectLog.getString("registrolog_inicio")+" - "+terminoLog+"")
                        }
                        adaptador?.notifyDataSetChanged()
                    }else{
                        Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
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
                params.put("var","var")
                params.put("rut", rut)
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }

    fun actualizarLogout(registro:Int){
        val solicitudString = object : StringRequest(
            Request.Method.POST, ApiCalls.call_clogin,
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
                params.put("var","var")
                params.put("registro", registro.toString())
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_perfil,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home->{
                finish()
                return true
            }
            R.id.mLogOut->{
                actualizarLogout(sharedPreferences?.obtenerInt("register_log")!!)
                sharedPreferences?.limpiarPreferencias()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                return true
            }
            else->{return super.onOptionsItemSelected(item)}
        }
    }
}
