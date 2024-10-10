package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.data.repositories.DetalleRestauranteRepository
import com.barapp.databinding.ItemRecyclerViewResultadosBusquedaBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.pantallas.PantallaMisFavoritos
import com.barapp.ui.recyclerViewAdapters.ResultadosRestauranteRecyclerAdapter.RestauranteViewHolder
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.DetalleRestaurante
import com.barapp.util.RestauranteUtils.getRealIdRestaurante
import com.barapp.util.diffCallbacks.RestauranteDiffCallback
import com.barapp.util.interfaces.LoadingHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.schedulers.Schedulers

class ResultadosRestauranteRecyclerAdapter(
  private val restaurantes: MutableList<Restaurante>,
  private val usuario: Usuario,
  private val handler: Callbacks,
  private val listener: OnItemClickListener,
  private val loadingHandler: LoadingHandler
) : RecyclerView.Adapter<RestauranteViewHolder>() {
  private var distancias: HashMap<String, Int?>
  private val detalleUsuarioRepository: DetalleUsuarioRepository
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository
  private val detalleRestauranteRepo: DetalleRestauranteRepository

  init {
    distancias = HashMap()
    detalleUsuarioRepository = DetalleUsuarioRepository.instance
    restauranteFavoritoRepository = RestauranteFavoritoRepository.instance
    detalleRestauranteRepo = DetalleRestauranteRepository.instance
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
    if (restaurante.cantidadOpiniones == 0) {
      holder.linearLayoutOpiniones.visibility = View.GONE
    } else {
      holder.puntuacionRestaurante.text = String.format(restaurante.puntuacion.toString().substring(0, 3))
      holder.ratingBarPuntuacion.rating = restaurante.puntuacion.toFloat()
      "(${restaurante.cantidadOpiniones})".also { holder.cantidadOpiniones.text = it }
    }
    Glide.with(holder.root.context)
      .load(restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(holder.logoRestaurante)
    //holder.imagenEstrella.setImageResource(R.drawable.icon_filled_star_24)

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
    holder.layoutCardView.transitionName = restaurante.id
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
    restauranteFavoritoRepository.guardar(restauranteFavorito, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        usuario.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
      }

      override fun onError(exception: Throwable) {}
    })
  }

  private fun eliminarFavorito(restaurante: Restaurante, position: Int) {
    restauranteFavoritoRepository.borrar(getRealIdRestaurante(restaurante), usuario.id, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        usuario.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
      }

      override fun onError(exception: Throwable) {}
    })
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
    val layoutCardView: ConstraintLayout
    val nombreRestaurante: TextView
    val ubicacionRestaurante: TextView
    val distanciaRestaurante: TextView
    val logoRestaurante: ImageView
    val botonFavorito: MaterialButton
    val linearLayoutOpiniones: View
    val puntuacionRestaurante: TextView
    val ratingBarPuntuacion: RatingBar
    val cantidadOpiniones: TextView
    val root: View
    val listener: OnItemClickListener

    init {
      root = binding.root
      card = binding.card
      layoutCardView = binding.layoutCardView
      logoRestaurante = binding.imageViewLogo
      nombreRestaurante = binding.txtViewNombreRestaurante
      ubicacionRestaurante = binding.txtViewUbicacionRestaurante
      distanciaRestaurante = binding.txtViewDistanciaRestaurante
      botonFavorito = binding.botonFavorito
      linearLayoutOpiniones = binding.linearLayoutOpiniones
      puntuacionRestaurante = binding.txtViewPuntuacionRestaurante
      ratingBarPuntuacion = binding.ratingBarPuntuacion
      cantidadOpiniones = binding.txtViewCantidadOpiniones
      card.setOnClickListener(this)
      this.listener = listener
    }

    override fun onClick(v: View) {
      val restauranteSeleccionado = restaurantes[bindingAdapterPosition]
      buscarDetalleRestaurante(restauranteSeleccionado)
    }

    private fun buscarDetalleRestaurante(restaurante: Restaurante) {
      loadingHandler.setLoading(true)

      detalleRestauranteRepo.buscarPorId(
        getRealIdRestaurante(restaurante),
        object : FirestoreCallback<DetalleRestaurante> {
          override fun onSuccess(result: DetalleRestaurante) {
            restaurante.detalleRestaurante = result
            listener.onClick(layoutCardView, restaurante, distancias[restaurante.id])
            loadingHandler.setLoading(false)
          }

          override fun onError(exception: Throwable) {
            mostrarErrorAlgoSalioMal(root.context.getString(R.string.error_buscando_detalle_restaurante, restaurante.nombre))
            loadingHandler.setLoading(false)
          }
        },
      )
    }

    // Si el restaurante posee idRestaurante es un RestauranteUsuario (favorito o vistoRecientemente)
    private fun getRealIdRestaurante(restaurante: Restaurante): String {
      return if (restaurante.idRestaurante != "") {
        restaurante.idRestaurante
      } else {
        restaurante.id
      }
    }

    private fun mostrarErrorAlgoSalioMal(message: String) {
      MaterialAlertDialogBuilder(root.context)
        .setIcon(R.drawable.icon_baseline_sentiment_very_dissatisfied_24)
        .setTitle(root.context.getString(R.string.error_titulo_algo_salio_mal))
        .setMessage(message)
        .setPositiveButton(root.context.getString(R.string.boton_aceptar), null)
        .show()
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
