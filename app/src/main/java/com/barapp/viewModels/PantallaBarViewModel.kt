package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.barapp.model.Reserva
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.data.repositories.ReservaRepository
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.util.RestauranteUtils.getRealIdRestaurante

class PantallaBarViewModel(var usuario: Usuario) : ViewModel() {
  var restaurante: Restaurante? = null

  private val restauranteFavoritoRepository = RestauranteFavoritoRepository.instance

  private val reservaRepository = ReservaRepository.instance

  private val _loading: MutableLiveData<Boolean> = MutableLiveData()
  val loading: LiveData<Boolean> = _loading

  private val _error: MutableLiveData<Throwable?> = MutableLiveData()
  val error: LiveData<Throwable?> = _error

  private val _loadingReservasPendientes: MutableLiveData<Boolean> = MutableLiveData(true)
  val loadingReservasPendientes: LiveData<Boolean> = _loadingReservasPendientes

  private val _alcanzoLimiteReservas: MutableLiveData<Boolean> = MutableLiveData()
  val alcanzoLimiteReservas: LiveData<Boolean> = _alcanzoLimiteReservas

  fun errorMostrado() {
    _error.value = null
  }

  fun hacerFavorito() {
    this._loading.value = true
    val restauranteFavorito = toRestauranteUsuario(restaurante!!)
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
    restauranteFavoritoRepository.borrar(getRealIdRestaurante(restaurante!!), usuario.id, usuario.idDetalleUsuario, object : FirestoreCallback<List<String>> {
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
    return (usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante!!.id) ||
        usuario.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante!!.idRestaurante))
  }

  fun buscarReservasPendientes() {
    reservaRepository.buscarUltimasReservasPendientes(restaurante!!.id, usuario.id, 3, object : FirestoreCallback<List<Reserva>> {
      override fun onSuccess(result: List<Reserva>) {
        _loadingReservasPendientes.postValue(false)
        _alcanzoLimiteReservas.postValue(result.size == 3)
      }

      override fun onError(exception: Throwable) {
        _loadingReservasPendientes.postValue(false)
        _error.postValue(exception)
      }
    })
  }

  class Factory(private val usuario: Usuario) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(PantallaBarViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST") return PantallaBarViewModel(usuario) as T
      }

      throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
    }
  }
}
