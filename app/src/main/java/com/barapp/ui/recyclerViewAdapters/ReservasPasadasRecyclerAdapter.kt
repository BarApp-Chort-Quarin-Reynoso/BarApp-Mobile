package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.databinding.ItemRecyclerViewReservasPasadasBinding
import com.barapp.barapp.model.Reserva
import com.barapp.ui.recyclerViewAdapters.ReservasPasadasRecyclerAdapter.ReservasPasadasViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ReservasPasadasRecyclerAdapter(private val reservas: MutableList<Reserva>) :
  RecyclerView.Adapter<ReservasPasadasViewHolder>() {
  inner class ReservasPasadasViewHolder(binding: ItemRecyclerViewReservasPasadasBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val card: CardView
    val imagenLogoRestaurante: ImageView
    val titulo: TextView
    val direccion: TextView
    val datosReserva: TextView
    val root: View

    init {
      root = binding.root
      card = binding.cardView
      imagenLogoRestaurante = binding.imageViewLogo
      titulo = binding.txtViewNombreRestaurante
      direccion = binding.txtViewDireccionRestaurante
      datosReserva = binding.txtViewDatosReserva
      binding.botonOpinar.setOnClickListener {
        Toast.makeText(this.root.getContext(), R.string.boton_opinar_accion, Toast.LENGTH_SHORT)
          .show()
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
    val ubicacion =
      (reserva.restaurante.ubicacion.calle + " " + reserva.restaurante.ubicacion.numero)
    val personaPluralOSingular = if (reserva.cantidadPersonas == 1) " persona" else " personas"
    val datosReserva =
      (reserva.cantidadPersonas.toString() +
        personaPluralOSingular +
        " | " +
        reserva.fecha.dayOfMonth +
        "/" +
        reserva.fecha.monthValue +
        " | " +
        reserva.horario.hora)
    reservaHolder.titulo.text = reserva.restaurante.nombre
    reservaHolder.direccion.text = ubicacion
    reservaHolder.datosReserva.text = datosReserva
    Glide.with(reservaHolder.root.context)
      .load(reserva.restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(reservaHolder.imagenLogoRestaurante)
  }

  override fun getItemCount(): Int {
    return reservas.size
  }

  fun setData(reservas: List<Reserva>?) {
    this.reservas.clear()
    this.reservas.addAll(reservas!!)
    notifyDataSetChanged()
  }
}
