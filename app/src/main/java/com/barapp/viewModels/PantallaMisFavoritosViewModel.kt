package com.barapp.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.util.Maps.Companion.calcularDistanciasABares
import timber.log.Timber

class PantallaMisFavoritosViewModel : ViewModel() {
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository =
    RestauranteFavoritoRepository.instance

  private var ubicacionUsuario: Location? = null
  private val _listaRestaurantesFavoritos = MutableLiveData<List<Restaurante>>()
  val listaRestaurantesFavoritos: LiveData<List<Restaurante>> = _listaRestaurantesFavoritos
  private val _distancias = MutableLiveData<HashMap<String, Int?>>(LinkedHashMap())
  val distancias: LiveData<HashMap<String, Int?>> = _distancias
  private val _cantidadFavoritos = MutableLiveData<Int>()
  val cantidadFavoritos: LiveData<Int> = _cantidadFavoritos
  private val _loading = MutableLiveData<Boolean>()
  val loading: LiveData<Boolean> = _loading
  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  init {
    _loading.value = true
  }

  fun buscarFavoritos(idUsuario: String) {
    _loading.value = true
    restauranteFavoritoRepository.buscarFavoritosDelUsuario(
      idUsuario,
      object : FirestoreCallback<List<Restaurante>> {
        override fun onSuccess(result: List<Restaurante>) {
          _loading.postValue(false)
          _listaRestaurantesFavoritos.postValue(result)
        }

        override fun onError(exception: Throwable) {
          _loading.postValue(false)
          _error.postValue(exception)
        }
      },
    )
  }

  fun calcularDistancias() {
    Timber.e("calcularDistancias ejecutado")
    if (listaRestaurantesFavoritos.value != null && ubicacionUsuario != null) {
      calcularDistanciasABares(listaRestaurantesFavoritos.value!!, ubicacionUsuario!!) {
        hashMap: HashMap<String, Int?> ->
        _distancias.postValue(hashMap)
      }
    }
  }

  fun ubicacionDisponible(ubicacionUsuario: Location?) {
    this.ubicacionUsuario = ubicacionUsuario
  }
}
