package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barapp.databinding.CaracteristicaLayoutBinding
import com.barapp.model.CalificacionPromedio

class CaracteristicasRecyclerAdapter(
    private var caracteristicas: Map<String, CalificacionPromedio>
) : RecyclerView.Adapter<CaracteristicasRecyclerAdapter.CaracteristicaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaracteristicaViewHolder {
        return CaracteristicaViewHolder(
            CaracteristicaLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
        )
    }

    override fun onBindViewHolder(holder: CaracteristicaViewHolder, position: Int) {
        val caracteristica = caracteristicas.toList()[position]
        holder.titulo.text = caracteristica.first
        if (caracteristica.second.cantidadOpiniones == 0) {
            holder.ratingBar.visibility = View.GONE
            holder.sinOpinionesAun.visibility = View.VISIBLE
        } else {
            holder.ratingBar.rating = caracteristica.second.puntuacion.toFloat()
        }
    }

    inner class CaracteristicaViewHolder(binding: CaracteristicaLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo: TextView
        val ratingBar: RatingBar
        val sinOpinionesAun: TextView

        init {
            titulo = binding.textViewTituloCaracteristica
            ratingBar = binding.ratingBarCaracteristica
            sinOpinionesAun = binding.textViewSinOpinionesAun
        }
    }

    override fun getItemCount(): Int {
        return caracteristicas.size
    }
}