package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteFavoritoRepository

class PantallaBarViewModel(var restaurante: Restaurante, var usuario: Usuario) : ViewModel() {
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository =
    RestauranteFavoritoRepository.instance

  private val _loading: MutableLiveData<Boolean> = MutableLiveData()
  val loading: LiveData<Boolean> = _loading

  private val _error: MutableLiveData<Throwable?> = MutableLiveData()
  val error: LiveData<Throwable?> = _error

  fun errorMostrado() {
    _error.value = null
  }

  fun hacerFavorito() {
    this._loading.value = true
    val restauranteFavorito = toRestauranteUsuario(restaurante)
    restauranteFavorito.idUsuario = usuario.id
    restauranteFavoritoRepository.guardar(restauranteFavorito, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        usuario.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
        _loading.postValue(false)
      }

      override fun onError(exception: Throwable) {
        _loading.postValue(false)
        _error.postValue(exception)
      }
    })
  }

  fun eliminarFavorito() {
    this._loading.value = true
    restauranteFavoritoRepository.borrar(restaurante.id, usuario.id, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        usuario.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
        _loading.postValue(false)
      }

      override fun onError(exception: Throwable) {
        _loading.postValue(false)
        _error.postValue(exception)
      }
    })
  }

  fun esFavorito(): Boolean {
    return (usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante.id) ||
        usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante.idRestaurante))
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
