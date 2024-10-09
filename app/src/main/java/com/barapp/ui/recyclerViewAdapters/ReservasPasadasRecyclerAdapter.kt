package com.barapp.ui.recyclerViewAdapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.databinding.ItemRecyclerViewReservasPasadasBinding
import com.barapp.barapp.model.Reserva
import com.barapp.model.EstadoReserva
import com.barapp.ui.recyclerViewAdapters.ReservasPasadasRecyclerAdapter.ReservasPasadasViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ReservasPasadasRecyclerAdapter(private val reservas: MutableList<Reserva>, private val listener: OnOpinarButtonClickListener) :
  RecyclerView.Adapter<ReservasPasadasViewHolder>() {
  inner class ReservasPasadasViewHolder(binding: ItemRecyclerViewReservasPasadasBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val card: CardView
    val imagenLogoRestaurante: ImageView
    val titulo: TextView
    val estado: TextView
    val direccion: TextView
    val datosReserva: TextView
    val botonOpinar: TextView
    val root: View

    init {
      root = binding.root
      card = binding.cardView
      imagenLogoRestaurante = binding.imageViewLogo
      titulo = binding.txtViewNombreRestaurante
      estado = binding.txtViewEstadoReserva
      direccion = binding.txtViewDireccionRestaurante
      datosReserva = binding.txtViewDatosReserva
      botonOpinar = binding.botonOpinar
      binding.botonOpinar.setOnClickListener {
        listener.onOpinarButtonClick(getBindingAdapterPosition())
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservasPasadasViewHolder {
    return ReservasPasadasViewHolder(
      ItemRecyclerViewReservasPasadasBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
      )
    )
  }

  override fun onBindViewHolder(reservaHolder: ReservasPasadasViewHolder, position: Int) {
    val reserva = reservas[position]
    val personaPluralOSingular = if (reserva.cantidadPersonas == 1) " persona" else " personas"
    val datosReserva =
      (reserva.cantidadPersonas.toString() +
        personaPluralOSingular +
        " | " +
        reserva.getFechaAsLocalDate().dayOfMonth +
        "/" +
        reserva.getFechaAsLocalDate().monthValue +
        " | " +
        reserva.horario.horario.substring(0,5))
    reservaHolder.titulo.text = reserva.restaurante.nombre
    val stateAndBackground = getTagEstadoReserva(reserva.estado, reservaHolder.root.context)
    reservaHolder.estado.text = stateAndBackground.first
    reservaHolder.estado.setTextColor(stateAndBackground.second)

    if (reserva.estado == EstadoReserva.CONCRETADA) {
        reservaHolder.botonOpinar.visibility = View.VISIBLE
    } else {
        reservaHolder.botonOpinar.visibility = View.INVISIBLE
    }

    if (reserva.idOpinion != null) {
        reservaHolder.botonOpinar.text = "¡Ya opinaste!"
        reservaHolder.botonOpinar.isEnabled = false
    }

    reservaHolder.datosReserva.text = datosReserva
    Glide.with(reservaHolder.root.context)
      .load(reserva.restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(reservaHolder.imagenLogoRestaurante)
  }

  private fun getTagEstadoReserva(estado: EstadoReserva, context: Context): Pair<String, Int> {
//    val drawable = when (estado) {
//      EstadoReserva.CONCRETADA -> context.getDrawable(R.drawable.tag_background_accepted_booking)
//      EstadoReserva.CANCELADA_USUARIO, EstadoReserva.CANCELADA_BAR, EstadoReserva.NO_ASISTIO -> context.getDrawable(R.drawable.tag_background_cancelled_booking)
//      EstadoReserva.PENDIENTE -> context.getDrawable(R.drawable.tag_background_pending_booking)
//    }
    // instead of drawable I want to return a color
    val color = when (estado) {
      EstadoReserva.CONCRETADA -> context.getColor(R.color.accepted_booking)
      EstadoReserva.CANCELADA_USUARIO, EstadoReserva.CANCELADA_BAR, EstadoReserva.NO_ASISTIO -> context.getColor(R.color.cancelled_booking)
      EstadoReserva.PENDIENTE -> context.getColor(R.color.pending_booking)
    }
    val text = when (estado) {
      EstadoReserva.CONCRETADA -> "CONCRETADA"
      EstadoReserva.CANCELADA_USUARIO, EstadoReserva.CANCELADA_BAR -> "CANCELADA"
      EstadoReserva.NO_ASISTIO -> "NO ASISTIÓ"
      EstadoReserva.PENDIENTE -> "PENDIENTE"
    }
    return Pair(text, color)
  }

  override fun getItemCount(): Int {
    return reservas.size
  }

  fun setData(reservas: List<Reserva>?) {
    this.reservas.clear()
    this.reservas.addAll(reservas!!)
    notifyDataSetChanged()
  }

  interface OnOpinarButtonClickListener {
    fun onOpinarButtonClick(position: Int)
  }
}
