package com.fabian.transporte

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fabian.transporte.Model.Marcador
import com.fabian.transporte.Model.Ruta
import com.fabian.transporte.Objects.ApiCalls
import com.fabian.transporte.Utils.AdaptadorCustom
import com.fabian.transporte.Utils.SharedPreference
import org.json.JSONException
import org.json.JSONObject

class ListarRutas : AppCompatActivity() {

    var listaRutas:RecyclerView? = null
    var layoutManager:RecyclerView.LayoutManager? = null
    var sharedPreferences: SharedPreference? = null
    companion object{
        var rutas = ArrayList<Ruta>()//rutas viejas
        var adaptador: AdaptadorCustom? = null

        fun obtenerRuta(index:Int): Ruta {
            return adaptador?.getItem(index) as Ruta
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_rutas)
        sharedPreferences = SharedPreference(this)


        var toolbar = findViewById<Toolbar>(R.id.ltoolbar)
        setSupportActionBar(toolbar)

        var actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        //Se Llena de la base de datos con la API con volley!
        rutas.clear()
        cargarRutas()
        //configuracion de lista
        listaRutas = findViewById(R.id.listaRutas)
        listaRutas?.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this) //LinearLayout
        listaRutas?.layoutManager = layoutManager
        adaptador = AdaptadorCustom(rutas, object : ClickListener {
            override fun onClick(vista: View, Index: Int) {
                //al hacer click utilizando la interface
                //enviamos a la vista del mapa para hacer ruteo
                var tipoRuta = tipoRuta(Index)
                if(tipoRuta==2){
                    val intent = Intent(applicationContext, sRuta::class.java)
                    intent.putExtra("ID", Index.toString())
                    startActivity(intent)
                }else{
                    val intent = Intent(applicationContext, lRuta::class.java)
                    intent.putExtra("ID",Index.toString())
                    startActivity(intent)
                }
            }
        })
        listaRutas?.adapter = adaptador
        //refresh
        val swipeToRefresh = findViewById<SwipeRefreshLayout>(R.id.refresh)
        swipeToRefresh.setOnRefreshListener {
            for (i in 1..100000000){} // solo count
            swipeToRefresh.isRefreshing = false
            Toast.makeText(this,"Recargando Rutas",Toast.LENGTH_SHORT).show()
            rutas.clear()
            cargarRutas()
        }
    }

    fun tipoRuta(index:Int):Int{
        var tipo:Int = 0
        var tipoString:String = rutas.get(index).tipo
        if(tipoString == "Libre"){
            tipo = 2
        }else{
            tipo = 1
        }
        return tipo
    }

    fun cargarRutas(){
        val stringRequest = StringRequest(Request.Method.GET,
            ApiCalls.call_routes,
            Response.Listener<String> { s ->
                try {
                    val obj = JSONObject(s)
                    if (!obj.getBoolean("error")) {
                        val array = obj.getJSONArray("rutas")
                        for (i in 0..array.length() - 1) {
                            val objectRuta = array.getJSONObject(i)
                            val ruta = Ruta(
                                objectRuta.getInt("ruta_id"),
                                objectRuta.getString("ruta_nombre"),
                                objectRuta.getString("ruta_descripcion"),
                                objectRuta.getString("tipo_ruta"),
                                Marcador(
                                    0,
                                    objectRuta.getDouble("ruta_inicio_lat"),
                                    objectRuta.getDouble("ruta_inicio_lon"),
                                    0
                                ),
                                Marcador(
                                    0,
                                    objectRuta.getDouble("ruta_fin_lat"),
                                    objectRuta.getDouble("ruta_fin_lon"),
                                    0
                                )
                            )
                            rutas.add(ruta)
                        }
                        adaptador?.notifyDataSetChanged()
                    } else {
                        Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { volleyError -> Toast.makeText(applicationContext, volleyError.message, Toast.LENGTH_LONG).show() })

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add<String>(stringRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lista,menu)
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


}
