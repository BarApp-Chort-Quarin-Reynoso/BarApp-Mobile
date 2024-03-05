package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.DetalleRestauranteRepository
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteFavoritoRepository

class PantallaBarViewModel(var restaurante: Restaurante, var usuario: Usuario) : ViewModel() {

  private val detalleRestauranteRepo: DetalleRestauranteRepository =
    DetalleRestauranteRepository.instance

  private val detalleUsuarioRepository: DetalleUsuarioRepository = DetalleUsuarioRepository.instance
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository =
    RestauranteFavoritoRepository.instance

  private val _detalleRestaurante: MutableLiveData<DetalleRestaurante> = MutableLiveData()
  val detalleRestaurante: LiveData<DetalleRestaurante> = _detalleRestaurante

  private val _loading: MutableLiveData<Boolean> = MutableLiveData()
  val loading: LiveData<Boolean> = _loading

  private val _error: MutableLiveData<Throwable?> = MutableLiveData()
  val error: LiveData<Throwable?> = _error

  init {
    buscarDetalleRestaurante()
  }

  private fun buscarDetalleRestaurante() {
    _loading.value = true

    detalleRestauranteRepo.buscarPorId(
      restaurante.idDetalleRestaurante,
      object : FirestoreCallback<DetalleRestaurante> {
        override fun onSuccess(result: DetalleRestaurante) {
          _loading.postValue(false)
          _detalleRestaurante.postValue(result)
          restaurante = restaurante.copy(result)
        }

        override fun onError(exception: Throwable) {
          _loading.postValue(false)
          _error.postValue(exception)
        }
      },
    )
  }

  fun errorMostrado() {
    _error.value = null
  }

  fun hacerFavorito() {
    usuario.detalleUsuario!!.idsRestaurantesFavoritos.add(restaurante.id)
    detalleUsuarioRepository.actualizarFavoritos(usuario.detalleUsuario!!)
    restauranteFavoritoRepository.guardar(restaurante, usuario.id)
  }

  fun eliminarFavorito() {
    usuario.detalleUsuario!!.idsRestaurantesFavoritos.remove(restaurante.id)
    detalleUsuarioRepository.actualizarFavoritos(usuario.detalleUsuario!!)
    restauranteFavoritoRepository.borrar(restaurante, usuario.id)
  }

  fun esFavorito(): Boolean {
    return usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante.id)
  }

  class Factory(private val restaurante: Restaurante, private val usuario: Usuario) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(PantallaBarViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST") return PantallaBarViewModel(restaurante, usuario) as T
      }

      throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
    }
  }
}
