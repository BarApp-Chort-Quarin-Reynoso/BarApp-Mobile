package com.barapp.ui.recyclerViewAdapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.data.repositories.DetalleRestauranteRepository
import com.barapp.databinding.ItemRecyclerViewHorizontalBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.recyclerViewAdapters.HorizontalRecyclerViewAdapter.RestaurantesViewHolder
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.DetalleRestaurante
import com.barapp.model.EstadoRestaurante
import com.barapp.util.RestauranteUtils.getRealIdRestaurante
import com.barapp.util.interfaces.LoadingHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import timber.log.Timber

class HorizontalRecyclerViewAdapter(
  private val listaRestaurantes: MutableList<Restaurante>,
  private val usuario: Usuario,
  private val handler: ActualizarFavoritos,
  private val idRecyclerView: String,
  private val listener: OnItemClickListener,
  private val loadingHandler: LoadingHandler
) : RecyclerView.Adapter<RestaurantesViewHolder>() {
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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantesViewHolder {
    return RestaurantesViewHolder(
      ItemRecyclerViewHorizontalBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false),
      listener,
    )
  }

  override fun onBindViewHolder(holder: RestaurantesViewHolder, position: Int) {
    val restaurante = listaRestaurantes[position]
    val calleNumero = restaurante.ubicacion.calle + " " + restaurante.ubicacion.numero
    holder.nombreRestaurante.text = restaurante.nombre
    holder.ubicacionRestaurante.text = calleNumero
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
    Glide.with(holder.root.context)
      .load(restaurante.portada)
      .apply(
        RequestOptions.bitmapTransform(
          MultiTransformation(
            CenterCrop(),
            RoundedCornersTransformation(40, 0, RoundedCornersTransformation.CornerType.TOP),
          )
        )
      )
      .into(holder.fotoRestaurante)

    listaRestaurantes[position].estado.let {
      if (it == EstadoRestaurante.PAUSADO) {
        holder.textoPausado.visibility = View.VISIBLE
        holder.fotoRestaurante.alpha = 0.5f
      } else {
        holder.textoPausado.visibility = View.GONE
        holder.fotoRestaurante.alpha = 1f
      }
    }

    if (
      usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(listaRestaurantes[position].id)
      ||
      usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(listaRestaurantes[position].idRestaurante)
    ) {
      holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
      holder.botonFavorito.isChecked = true
    } else {
      holder.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
      holder.botonFavorito.isChecked = false
    }
    holder.botonFavorito.setOnClickListener {
      if (holder.botonFavorito.isChecked) {
        holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
        agregarFavorito(listaRestaurantes[position], holder)
      } else {
        holder.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
        eliminarFavorito(listaRestaurantes[position], holder)
      }
    }
    if (distancias[restaurante.id] != null) {
      holder.distanciaRestaurante.visibility = View.VISIBLE
      holder.distanciaRestaurante.text =
        holder.root.context.getString(R.string.cardview_texto_distancia, distancias[restaurante.id])
    } else {
      holder.distanciaRestaurante.visibility = View.INVISIBLE
    }
    holder.layoutCardView.transitionName = idRecyclerView + restaurante.id
  }

  override fun onBindViewHolder(
    holder: RestaurantesViewHolder,
    position: Int,
    payloads: List<Any>,
  ) {
    if (payloads.isNotEmpty()) {
      if (payloads[0] is Int) {
        if (payloads[0] == CAMBIADA_DISTANCIA) {
          if (distancias[listaRestaurantes[position].id] != null) {
            holder.distanciaRestaurante.visibility = View.VISIBLE
            holder.distanciaRestaurante.text =
              holder.root.context.getString(
                R.string.cardview_texto_distancia,
                distancias[listaRestaurantes[position].id],
              )
          }
        } else {
          Timber.e("Payload: " + payloads[0].toString())
        }
      } else {
        Timber.e("Payload: " + payloads[0].toString())
      }
    } else {
      super.onBindViewHolder(holder, position, payloads)
    }
  }

  fun updateRestaurantesItems(newRestaurantes: List<Restaurante>) {
      this.listaRestaurantes.clear()
      this.listaRestaurantes.addAll(newRestaurantes)
  }

  @SuppressLint("NotifyDataSetChanged")
  fun addToFavorites(holder: RestaurantesViewHolder, idRestaurante: String) {
    for (restaurante in listaRestaurantes) {
      if (restaurante.id == idRestaurante || restaurante.idRestaurante == idRestaurante) {
        holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
        holder.botonFavorito.isChecked = true
        notifyDataSetChanged()
        break
      }
    }
  }

  fun setDistancias(distancias: HashMap<String, Int?>) {
    this.distancias = distancias
    notifyItemRangeChanged(0, distancias.size, CAMBIADA_DISTANCIA)
  }

  private fun agregarFavorito(restaurante: Restaurante, holder: RestaurantesViewHolder) {
    val restauranteFavorito = toRestauranteUsuario(restaurante)
    restauranteFavorito.idUsuario = usuario.id
    restauranteFavoritoRepository.guardar(restauranteFavorito, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        usuario.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
        handler.actualizarFavoritos(holder, getRealIdRestaurante(restaurante))
      }

      override fun onError(exception: Throwable) {}
    })
  }

  private fun eliminarFavorito(restaurante: Restaurante, holder: RestaurantesViewHolder) {
    restauranteFavoritoRepository.borrar(getRealIdRestaurante(restaurante), usuario.id, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        usuario.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
        handler.actualizarFavoritos(holder, getRealIdRestaurante(restaurante))
      }

      override fun onError(exception: Throwable) {}
    })
  }

  override fun getItemCount(): Int {
    return listaRestaurantes.size
  }

  // Clase interna que busca holdea los elementos necesarios
  inner class RestaurantesViewHolder(
    binding: ItemRecyclerViewHorizontalBinding,
    listener: OnItemClickListener,
  ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    val nombreRestaurante: TextView
    val ubicacionRestaurante: TextView
    val distanciaRestaurante: TextView
    val linearLayoutOpiniones: View
    val puntuacionRestaurante: TextView
    val ratingBarPuntuacion: RatingBar
    val cantidadOpiniones: TextView
    val textoPausado: TextView
    val logoRestaurante: ImageView
    val fotoRestaurante: ImageView
    val botonFavorito: MaterialButton
    val root: View
    val cardView: MaterialCardView
    val layoutCardView: ConstraintLayout
    private val listener: OnItemClickListener

    init {
      root = binding.root
      cardView = binding.cardView
      layoutCardView = binding.layoutCardView
      nombreRestaurante = binding.txtViewNombreRestaurante
      ubicacionRestaurante = binding.txtViewUbicacionRestaurante
      distanciaRestaurante = binding.txtViewDistanciaRestaurante
      linearLayoutOpiniones = binding.linearLayoutOpiniones
      puntuacionRestaurante = binding.txtViewPuntuacionRestaurante
      ratingBarPuntuacion = binding.ratingBarPuntuacion
      cantidadOpiniones = binding.txtViewCantidadOpiniones
      textoPausado = binding.txtViewPaused
      logoRestaurante = binding.imageViewLogo
      fotoRestaurante = binding.imageViewFoto
      botonFavorito = binding.botonFavorito
      cardView.setOnClickListener(this)
      this.listener = listener
    }

    override fun onClick(v: View) {
      val restauranteSeleccionado = listaRestaurantes[bindingAdapterPosition]
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

    private fun mostrarErrorAlgoSalioMal(message: String) {
      MaterialAlertDialogBuilder(root.context)
        .setIcon(R.drawable.icon_baseline_sentiment_very_dissatisfied_24)
        .setTitle(root.context.getString(R.string.error_titulo_algo_salio_mal))
        .setMessage(message)
        .setPositiveButton(root.context.getString(R.string.boton_aceptar), null)
        .show()
    }
  }

  interface OnItemClickListener {
    fun onClick(transitionView: View, restaurante: Restaurante, distancia: Int?)
  }

  interface ActualizarFavoritos {
    fun actualizarFavoritos(holder: RestaurantesViewHolder, idRestaurante: String)
  }

  companion object {
    private const val CAMBIADA_DISTANCIA = 1
  }
}
