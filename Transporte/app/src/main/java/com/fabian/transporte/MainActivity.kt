package com.fabian.transporte

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.fabian.transporte.Model.Conductor
import com.fabian.transporte.Objects.ApiCalls
import com.fabian.transporte.Utils.SharedPreference
import com.fabian.transporte.Utils.VolleySingleton
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {


    var sharedPreferences: SharedPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var internet = checkInternet()
        if(!internet){
            Toast.makeText(this,"APK sin Internet",Toast.LENGTH_LONG).show()
            finish()
        }
        sharedPreferences = SharedPreference(this)
        var validarLogin = sharedPreferences?.obtenerInt("login")
        if(validarLogin == 1){
            val intent = Intent(this,Inicio::class.java)
            startActivity(intent)
        }
        var textRut = findViewById<EditText>(R.id.tx_rut)
        var textPass = findViewById<EditText>(R.id.tx_pass)
        val btnLog = findViewById<Button>(R.id.btn_login)
        btnLog?.setOnClickListener {


            var rut = textRut.text
            var pass = textPass.text
            if(rut.isEmpty() || pass.isEmpty()){
                Toast.makeText(applicationContext,"Campos Vacios",Toast.LENGTH_SHORT).show()
            }else{
                if(rut.length>=8){
                    var rutformat:String
                    var x = 0
                    if(rut.length == 12){
                        rutformat = rut.toString()
                        x++
                    }else if(rut.length == 9){
                        rutformat =  formatearrut(rut.toString())
                        x++
                    }else{
                        rutformat = ""
                        Toast.makeText(applicationContext,"Rut Mal Escrito",Toast.LENGTH_SHORT).show()
                    }
                    if(x>0){
                        if(validarRut(rutformat)){
                            validarConductor(rutformat,pass.toString())
                        }else{
                            Toast.makeText(applicationContext,"Rut Invalido",Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(applicationContext,"Minimos 8 carácteres para el rut",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun validarConductor(rut:String,pass:String){
        val solicitudString = object : StringRequest(Request.Method.POST,
            ApiCalls.call_login,Response.Listener<String>{
                    response ->
                try {
                    var obj = JSONObject(response)
                    if(!obj.getBoolean("error")){
                        if(obj.getInt("result")==1){
                            val array = obj.getJSONArray("driver")
                            val objectConductor =   array.getJSONObject(0)
                            val conductor = Conductor(
                                objectConductor.getString("rut"),
                                objectConductor.getString("nombre"),
                                objectConductor.getString("apellido"),
                                "S/T",
                                "S/C"
                            )
                            var log:Int = obj.getInt("Log")

                            sharedPreferences?.guardar("rut_usuario",conductor.rut)
                            sharedPreferences?.guardar("nombre_usuario",conductor.nombre)
                            sharedPreferences?.guardar("apellido_usuario",conductor.apellido)
                            sharedPreferences?.guardar("register_log",log)
                            sharedPreferences?.guardar("fecha_ingreso",obj.getString("datetime"))
                            sharedPreferences?.guardar("login",1)

                            val intent = Intent(this,Inicio::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(applicationContext,"Error de Token - No se pudo validar token - Login Denegado ",Toast.LENGTH_SHORT).show()
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
                params.put("rut", rut)
                params.put("password", pass)
                return params
            }
        }
        VolleySingleton.instancia?.addSolicitudVolley(solicitudString)
    }

    fun checkInternet(): Boolean {
        val cm  = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork!=null && activeNetwork.isConnected
    }

    fun validarRut(rut:String):Boolean{
        if (!rut.matches(Regex("[0-9]{1,2}(.?[0-9]{3}){2}-?[0-9kK]"))) {
            return false
        }
        // Very cool code :-)  THANKS
        val rutf = rut.toLowerCase().replace(Regex("[-.]"), "")
        val rutl = rutf.takeLast(1); var sum = 0; var i = 0
        for (x in rutf.dropLast(1).reversed()){
            sum += (x.toString().toInt() * ((i % 6) + 2)); i+=1
        }

        val div = 11 - (sum % 11)
        return (if (div == 11) 0 else div) == (if (rutl == "k") 10 else rutl.toInt())
    }

    fun formatearrut(rut:String):String{
        var rutformat:String
        var cont:Int = 0
        var ruttmp = rut
        ruttmp = ruttmp.replace(".","")
        ruttmp = ruttmp.replace("-","")
        rutformat = "-" + ruttmp.substring(rut.length - 1)
        var i:Int = rut.length - 2
        while (i>=0){
            rutformat = rut.substring(i,i+1) + rutformat
            cont++
            if(cont == 3 && i!=0){
                rutformat = "."+ rutformat
                cont = 0
            }
            i--
        }
        return rutformat
    }
}
