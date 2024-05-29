package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barapp.databinding.OpinionLayoutBinding
import com.barapp.model.Opinion
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

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
        holder.opinion.text = opinion.comentario
        holder.rating.rating = opinion.nota.toFloat()
        Glide.with(holder.itemView.context)
            .load(opinion.usuario.foto)
            .into(holder.foto)
    }

    override fun getItemCount(): Int {
        return opiniones.size
    }

    inner class OpinionViewHolder(binding: OpinionLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val usuario: TextView
        val opinion: TextView
        val rating: RatingBar
        val foto: ShapeableImageView

        init {
            usuario = binding.textViewUsuario
            opinion = binding.textViewOpinion
            rating = binding.ratingBarPuntuacion
            foto = binding.imageViewFotoPerfil
        }
    }

    interface Callbacks {
        fun actualizarCantidadOpiniones(cantRestaurantes: Int)
    }
}