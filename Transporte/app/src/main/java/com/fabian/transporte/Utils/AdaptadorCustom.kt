package com.fabian.transporte.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fabian.transporte.ClickListener
import com.fabian.transporte.Model.Ruta
import com.fabian.transporte.R

class AdaptadorCustom(items:ArrayList<Ruta>, var listener: ClickListener): RecyclerView.Adapter<AdaptadorCustom.ViewHolder>(){

    var items:ArrayList<Ruta>? = null
    init {
        this.items = items
    }
    //Metodos Propios
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.template_rutas,parent,false)
        val viewHolder = ViewHolder(vista, listener)
        return viewHolder
        //crea el viewHolder y coloca el archivo xml a la vista
    }

    fun getItem(p0: Int): Any {
        return this.items?.get(p0)!!
    }

    override fun getItemCount(): Int {
       return items?.count()!!
        //conteo de elementos
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items?.get(position)
        holder.nombre?.text = item?.nombre
        holder.descripcion?.text = item?.descripcion
        holder.tipo?.text = item?.tipo
        //mapeo  de elementos
    }
    //clase ViewHolder
    class ViewHolder(vista: View, listener: ClickListener): RecyclerView.ViewHolder(vista), View.OnClickListener{
        var vista = vista
        var nombre:TextView? = null
        var descripcion:TextView? = null
        var tipo:TextView? = null
        var listener: ClickListener? = null
        init {
            nombre = vista.findViewById(R.id.tv_nombreRuta)
            descripcion = vista.findViewById(R.id.tv_descripcionRuta)
            tipo = vista.findViewById(R.id.tv_tipo)
            this.listener = listener
            vista.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            //reescribir funcion
            this.listener?.onClick(p0!!,adapterPosition)
        }
    }

}