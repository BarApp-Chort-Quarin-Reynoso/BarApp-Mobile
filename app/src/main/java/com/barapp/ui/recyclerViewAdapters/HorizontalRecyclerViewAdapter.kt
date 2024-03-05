package com.barapp.ui.recyclerViewAdapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.databinding.ItemRecyclerViewHorizontalBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.recyclerViewAdapters.HorizontalRecyclerViewAdapter.RestaurantesViewHolder
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.util.diffCallbacks.RestauranteDiffCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.core.SingleOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import timber.log.Timber

class HorizontalRecyclerViewAdapter(
  private val listaRestaurantes: MutableList<Restaurante>,
  private val usuario: Usuario,
  private val handler: ActualizarFavoritos,
  private val idRecyclerView: String,
  private val listener: OnItemClickListener,
) : RecyclerView.Adapter<RestaurantesViewHolder>() {
  private var distancias: HashMap<String, Int?>
  private val detalleUsuarioRepository: DetalleUsuarioRepository
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository

  init {
    distancias = HashMap()
    detalleUsuarioRepository = DetalleUsuarioRepository.instance
    restauranteFavoritoRepository = RestauranteFavoritoRepository.instance
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantesViewHolder {
    return RestaurantesViewHolder(
      ItemRecyclerViewHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false),
      listener,
    )
  }

  override fun onBindViewHolder(holder: RestaurantesViewHolder, position: Int) {
    val restaurante = listaRestaurantes[position]
    val calleNumero = restaurante.ubicacion.calle + " " + restaurante.ubicacion.numero
    holder.nombreRestaurante.text = restaurante.nombre
    holder.ubicacionRestaurante.text = calleNumero
    holder.puntuacionRestaurante.text = String.format(restaurante.puntuacion.toString())
    Glide.with(holder.root.context)
      .load(restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(holder.logoRestaurante)
    Glide.with(holder.root.context)
      .load(restaurante.foto)
      .apply(
        RequestOptions.bitmapTransform(
          MultiTransformation(
            CenterCrop(),
            RoundedCornersTransformation(40, 0, RoundedCornersTransformation.CornerType.TOP),
          )
        )
      )
      .into(holder.fotoRestaurante)

    // Si el detalle usuario asociado al usuario contiene el id restaurante poner el corazon lleno
    if (
      usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(listaRestaurantes[position].id)
    ) {
      holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
      holder.botonFavorito.isChecked = true
    } else {
      holder.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
      holder.botonFavorito.isChecked = false
    }
    holder.botonFavorito.setOnClickListener {
      if (holder.botonFavorito.isChecked) {
        // Logica de agregar favorito
        holder.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
        hacerFavorito(listaRestaurantes[position])
      } else {
        // Logica de remover favorito
        holder.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
        eliminarFavorito(listaRestaurantes[position])
      }
      handler.actualizarFavoritos()
    }
    if (distancias[restaurante.id] != null) {
      holder.distanciaRestaurante.visibility = View.VISIBLE
      holder.distanciaRestaurante.text =
        holder.root.context.getString(R.string.cardview_texto_distancia, distancias[restaurante.id])
    } else {
      holder.distanciaRestaurante.visibility = View.INVISIBLE
    }
    holder.cardView.transitionName = idRecyclerView + restaurante.id
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
    val disposable =
      Single.create { emitter: SingleEmitter<DiffResult> ->
        val diffCallback = RestauranteDiffCallback(listaRestaurantes, newRestaurantes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        emitter.onSuccess(diffResult)
      }
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { result: DiffResult ->
          listaRestaurantes.clear()
          listaRestaurantes.addAll(newRestaurantes)
          result.dispatchUpdatesTo(this)
        }
  }

  fun setDistancias(distancias: HashMap<String, Int?>) {
    this.distancias = distancias
    notifyItemRangeChanged(0, distancias.size, CAMBIADA_DISTANCIA)
  }

  private fun hacerFavorito(restaurante: Restaurante) {
    usuario.detalleUsuario!!.idsRestaurantesFavoritos.add(restaurante.id)
    detalleUsuarioRepository.actualizarFavoritos(usuario.detalleUsuario!!)
    restauranteFavoritoRepository.guardar(restaurante, usuario.id)
  }

  private fun eliminarFavorito(restaurante: Restaurante) {
    usuario.detalleUsuario!!.idsRestaurantesFavoritos.remove(restaurante.id)
    detalleUsuarioRepository.actualizarFavoritos(usuario.detalleUsuario!!)
    restauranteFavoritoRepository.borrar(restaurante, usuario.id)
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
    val puntuacionRestaurante: TextView
    val logoRestaurante: ImageView
    val fotoRestaurante: ImageView
    val botonFavorito: MaterialButton
    val root: View
    val cardView: MaterialCardView
    private val listener: OnItemClickListener

    init {
      root = binding.root
      cardView = binding.cardView
      nombreRestaurante = binding.txtViewNombreRestaurante
      ubicacionRestaurante = binding.txtViewUbicacionRestaurante
      distanciaRestaurante = binding.txtViewDistanciaRestaurante
      puntuacionRestaurante = binding.txtViewPuntuacionRestaurante
      logoRestaurante = binding.imageViewLogo
      fotoRestaurante = binding.imageViewFoto
      botonFavorito = binding.botonFavorito
      cardView.setOnClickListener(this)
      this.listener = listener
    }

    override fun onClick(v: View) {
      val r = listaRestaurantes[bindingAdapterPosition]
      this.listener.onClick(cardView, r, distancias[r.id])
    }
  }

  interface OnItemClickListener {
    fun onClick(transitionView: View, restaurante: Restaurante, distancia: Int?)
  }

  interface ActualizarFavoritos {
    fun actualizarFavoritos()
  }

  companion object {
    private const val CAMBIADA_DISTANCIA = 1
  }
}
