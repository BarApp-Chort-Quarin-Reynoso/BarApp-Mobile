package com.barapp.ui.recyclerViewAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barapp.databinding.OpinionLayoutBinding
import com.barapp.model.Opinion
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class OpinionesRecyclerAdapter (private val opiniones : MutableList<Opinion>) : RecyclerView.Adapter<OpinionesRecyclerAdapter.OpinionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpinionViewHolder {
        return OpinionViewHolder(
            OpinionLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
        )
    }

    override fun onBindViewHolder(holder: OpinionViewHolder, position: Int) {
        val opinion = opiniones[position]
        val nombreCompleto = opinion.usuario.nombre + " " + opinion.usuario.apellido
        holder.usuario.text = nombreCompleto
        holder.fecha.text = getFechaFormateada(opinion.fecha)
        holder.opinion.text = opinion.comentario
        if (opinion.comentario == "") {
            holder.opinion.visibility = View.INVISIBLE
        }
        holder.rating.rating = opinion.nota.toFloat()
        holder.cantidadPersonas.text = getTextoCantidadPersonas(opinion.cantidadPersonas)
        "(${opinion.horario.tipoComida})".also { holder.tipoComida.text = it }
        Glide.with(holder.itemView.context)
            .load(opinion.usuario.foto)
            .into(holder.foto)
    }

    private fun getTextoCantidadPersonas(cantidadPersonas: Int): String {
        return "Reservó para " + if (cantidadPersonas == 1) {
            "1 persona"
        } else {
            "$cantidadPersonas personas"
        }
    }

    private fun getFechaFormateada(fecha: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
        val date = inputFormat.parse(fecha)
        return outputFormat.format(date!!)
    }

    override fun getItemCount(): Int {
        return opiniones.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateOpiniones(opiniones: List<Opinion>) {
        this.opiniones.clear()
        this.opiniones.addAll(opiniones)
        notifyDataSetChanged()
    }

    inner class OpinionViewHolder(binding: OpinionLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val usuario: TextView
        val fecha: TextView
        val opinion: TextView
        val rating: RatingBar
        val cantidadPersonas: TextView
        val tipoComida: TextView
        val foto: ShapeableImageView

        init {
            usuario = binding.textViewUsuario
            fecha = binding.textViewFecha
            opinion = binding.textViewOpinion
            rating = binding.ratingBarPuntuacion
            cantidadPersonas = binding.textViewCantidadPersonas
            tipoComida = binding.textViewTipoComida
            foto = binding.imageViewFotoPerfil
        }
    }
}