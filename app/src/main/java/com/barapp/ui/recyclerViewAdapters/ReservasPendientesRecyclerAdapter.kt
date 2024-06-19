package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.barapp.databinding.ItemRecyclerViewReservasPendientesBinding
import com.barapp.barapp.model.Reserva
import com.barapp.ui.recyclerViewAdapters.ReservasPendientesRecyclerAdapter.ReservasPendientesViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class ReservasPendientesRecyclerAdapter(
  private val reservas: MutableList<Reserva>,
  private val listener: OnItemClickListener,
) : RecyclerView.Adapter<ReservasPendientesViewHolder>() {
  inner class ReservasPendientesViewHolder(
    binding: ItemRecyclerViewReservasPendientesBinding,
    listener: OnItemClickListener,
  ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    val card: CardView
    val imagenRestaurante: ImageView
    val imagenLogoRestaurante: ImageView
    val titulo: TextView
    val ubicacion: TextView
    val datosReserva: TextView
    val root: View
    val listener: OnItemClickListener

    init {
      root = binding.root
      card = binding.cardView
      imagenRestaurante = binding.imageViewFoto
      imagenLogoRestaurante = binding.imageViewLogo
      titulo = binding.txtViewNombreRestaurante
      ubicacion = binding.txtViewUbicacionRestaurante
      datosReserva = binding.txtViewDatosReserva
      card.setOnClickListener(this)
      this.listener = listener
    }

    override fun onClick(v: View) {
      this.listener.onClick(card, reservas[bindingAdapterPosition])
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservasPendientesViewHolder {
    return ReservasPendientesViewHolder(
      ItemRecyclerViewReservasPendientesBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
      ),
      listener,
    )
  }

  override fun onBindViewHolder(reservaHolder: ReservasPendientesViewHolder, position: Int) {
    val reserva = reservas[position]
//    val ubicacion =
//      (reserva.restaurante.ubicacion.calle + " " + reserva.restaurante.ubicacion.numero)
    val personaPluralOSingular = if (reserva.cantidadPersonas == 1) " persona" else " personas"
    val datosReserva =
      (reserva.cantidadPersonas.toString() +
        personaPluralOSingular +
        " | " +
        reserva.getFechaAsLocalDate().dayOfMonth +
        "/" +
        reserva.getFechaAsLocalDate().monthValue +
        " | " +
        reserva.horario.hora.toString())
    reservaHolder.titulo.text = reserva.restaurante.nombre
//    reservaHolder.ubicacion.text = ubicacion
    reservaHolder.datosReserva.text = datosReserva
    Glide.with(reservaHolder.root.context)
      .load(reserva.restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(reservaHolder.imagenLogoRestaurante)
    Glide.with(reservaHolder.root.context)
      .load(reserva.restaurante.foto) // .apply(RequestOptions.centerCropTransform())
      .apply(
        RequestOptions.bitmapTransform(
          MultiTransformation(
            CenterCrop(),
            RoundedCornersTransformation(40, 0, RoundedCornersTransformation.CornerType.TOP),
          )
        )
      )
      .into(reservaHolder.imagenRestaurante)
    reservaHolder.card.transitionName = reserva.restaurante.id
  }

  override fun getItemCount(): Int {
    return reservas.size
  }

  fun setData(r: List<Reserva>?) {
    reservas.clear()
    reservas.addAll(r!!)
    notifyDataSetChanged()
  }

  interface OnItemClickListener {
    fun onClick(transitionView: View, reserva: Reserva)
  }
}
