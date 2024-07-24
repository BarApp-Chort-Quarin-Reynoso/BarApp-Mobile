package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.databinding.ItemRecyclerViewResultadosBusquedaBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.pantallas.PantallaMisFavoritos
import com.barapp.ui.recyclerViewAdapters.ResultadosRestauranteRecyclerAdapter.RestauranteViewHolder
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.util.diffCallbacks.RestauranteDiffCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.core.SingleOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers

class ResultadosRestauranteRecyclerAdapter(
  private val restaurantes: MutableList<Restaurante>,
  private val usuario: Usuario,
  private val handler: Callbacks,
  private val listener: OnItemClickListener,
) : RecyclerView.Adapter<RestauranteViewHolder>() {
  private var distancias: HashMap<String, Int?>
  private val detalleUsuarioRepository: DetalleUsuarioRepository
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository

  init {
    distancias = HashMap()
    detalleUsuarioRepository = DetalleUsuarioRepository.instance
    restauranteFavoritoRepository = RestauranteFavoritoRepository.instance
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestauranteViewHolder {
    return RestauranteViewHolder(
      ItemRecyclerViewResultadosBusquedaBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
      ),
      listener,
    )
  }

  override fun onBindViewHolder(holder: RestauranteViewHolder, position: Int) {
    val restaurante = restaurantes[position]
    val ubicacion = restaurante.ubicacion.calle + " " + restaurante.ubicacion.numero
    holder.nombreRestaurante.text = restaurante.nombre
    holder.ubicacionRestaurante.text = ubicacion
    holder.puntuacionRestaurante.text = String.format(restaurante.puntuacion.toString())
    Glide.with(holder.root.context)
      .load(restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(holder.logoRestaurante)
    holder.imagenEstrella.setImageResource(R.drawable.icon_filled_star_24)

    if (usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurantes[position].idRestaurante)) {
      holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
      holder.botonFavorito.isChecked = true
    } else {
      holder.botonFavorito.setIconResource(R.drawable.icon_outlined_star_24)
      holder.botonFavorito.isChecked = false
    }
    holder.botonFavorito.setOnClickListener {
      if (holder.botonFavorito.isChecked) {
        holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
        hacerFavorito(restaurantes[position])
      } else {
        holder.botonFavorito.setIconResource(R.drawable.icon_outlined_star_24)
        eliminarFavorito(restaurantes[position], position)
      }
    }
    if (distancias[restaurante.id] != null) {
      holder.distanciaRestaurante.visibility = View.VISIBLE
      holder.distanciaRestaurante.text =
        holder.card.context.getString(R.string.cardview_texto_distancia, distancias[restaurante.id])
    } else {
      holder.distanciaRestaurante.visibility = View.INVISIBLE
    }
    holder.card.transitionName = restaurante.id
  }

  override fun onBindViewHolder(holder: RestauranteViewHolder, position: Int, payloads: List<Any>) {
    if (payloads.isNotEmpty()) {
      if (payloads[0] is Int) {
        if (payloads[0] == CAMBIADA_DISTANCIA) {
          if (distancias[restaurantes[position].id] != null) {
            holder.distanciaRestaurante.visibility = View.VISIBLE
            holder.distanciaRestaurante.text =
              holder.card.context.getString(
                R.string.cardview_texto_distancia,
                distancias[restaurantes[position].id],
              )
          }
        }
      }
    } else {
      super.onBindViewHolder(holder, position, payloads)
    }
  }

  private fun hacerFavorito(restaurante: Restaurante) {
    val restauranteFavorito = toRestauranteUsuario(restaurante)
    restauranteFavorito.idUsuario = usuario.id
    usuario.detalleUsuario!!.idsRestaurantesFavoritos.add(restauranteFavorito.idRestaurante)
    detalleUsuarioRepository.actualizarFavoritos(usuario.detalleUsuario!!)
    restauranteFavoritoRepository.guardar(restauranteFavorito, usuario.id)
  }

  private fun eliminarFavorito(restaurante: Restaurante, position: Int) {
    usuario.detalleUsuario!!.idsRestaurantesFavoritos.remove(restaurante.idRestaurante)
    detalleUsuarioRepository.actualizarFavoritos(usuario.detalleUsuario!!)
    restauranteFavoritoRepository.borrar(restaurante)
    if (handler is PantallaMisFavoritos) {
      restaurantes.remove(restaurante)
      notifyItemRemoved(position)
      notifyItemRangeChanged(position, restaurantes.size)
      handler.actualizarCantidadRestaurantes(this.itemCount)
    }
  }

  fun updateRestaurantesItems(newRestaurantes: List<Restaurante>?) {
    val disposable =
      Single.create { emitter: SingleEmitter<DiffResult> ->
        val diffCallback = RestauranteDiffCallback(restaurantes, newRestaurantes!!)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        emitter.onSuccess(diffResult)
      }
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { result: DiffResult ->
          restaurantes.clear()
          restaurantes.addAll(newRestaurantes!!)
          result.dispatchUpdatesTo(this)
        }
  }

  fun setDistancias(distancias: HashMap<String, Int?>) {
    this.distancias = distancias
    notifyItemRangeChanged(0, distancias.size, CAMBIADA_DISTANCIA)
  }

  override fun getItemCount(): Int {
    return restaurantes.size
  }

  inner class RestauranteViewHolder(
    binding: ItemRecyclerViewResultadosBusquedaBinding,
    listener: OnItemClickListener,
  ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    val card: CardView
    val nombreRestaurante: TextView
    val ubicacionRestaurante: TextView
    val distanciaRestaurante: TextView
    val logoRestaurante: ImageView
    val botonFavorito: MaterialButton
    val puntuacionRestaurante: TextView
    val imagenEstrella: ImageView
    val root: View
    val listener: OnItemClickListener

    init {
      root = binding.root
      card = binding.card
      logoRestaurante = binding.imageViewLogo
      nombreRestaurante = binding.txtViewNombreRestaurante
      ubicacionRestaurante = binding.txtViewUbicacionRestaurante
      distanciaRestaurante = binding.txtViewDistanciaRestaurante
      botonFavorito = binding.botonFavorito
      puntuacionRestaurante = binding.txtViewPuntuacionRestaurante
      imagenEstrella = binding.imageViewEstrella
      card.setOnClickListener(this)
      this.listener = listener
    }

    override fun onClick(v: View) {
      this.listener.onClick(
        card,
        restaurantes[bindingAdapterPosition],
        distancias[restaurantes[bindingAdapterPosition].id],
      )
    }
  }

  interface Callbacks {
    fun actualizarCantidadRestaurantes(cantRestaurantes: Int)
  }

  interface OnItemClickListener {
    fun onClick(transitionView: View, restaurante: Restaurante, distancia: Int?)
  }

  companion object {
    private const val CAMBIADA_DISTANCIA = 1
  }
}
