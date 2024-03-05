package com.barapp.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.util.Maps.Companion.calcularDistanciasABares
import java.util.Locale
import java.util.stream.Collectors
import timber.log.Timber

class PantallaResultadosBusquedaViewModel : ViewModel() {
  private val restauranteRepository: RestauranteRepository = RestauranteRepository.instance

  private var ubicacionUsuario: Location? = null
  private val _listaRestaurantes = MutableLiveData<List<Restaurante>>()
  val listaRestaurantes: LiveData<List<Restaurante>> = _listaRestaurantes
  private val _distancias = MutableLiveData<HashMap<String, Int?>>(LinkedHashMap())
  val distancias: LiveData<HashMap<String, Int?>> = _distancias
  private val _loading = MutableLiveData<Boolean>()
  val loading: LiveData<Boolean> = _loading
  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  fun buscarRestaurantesSegunTexto(textoBusqueda: String) {
    if (_listaRestaurantes.value != null) return
    restauranteRepository.buscarTodos(
      object : FirestoreCallback<List<Restaurante>> {
        override fun onSuccess(result: List<Restaurante>) {
          _listaRestaurantes.postValue(
            result
              .stream()
              .filter { restaurante: Restaurante ->
                restaurante.nombre
                  .uppercase(Locale.getDefault())
                  .contains(textoBusqueda.uppercase(Locale.getDefault()))
              }
              .collect(Collectors.toList())
          )
        }

        override fun onError(exception: Throwable) {
          _error.postValue(exception)
        }
      }
    )
  }

  fun calcularDistancias() {
    Timber.e("calcularDistancias ejecutado")
    if (listaRestaurantes.value != null && ubicacionUsuario != null) {
      val subscription =
        calcularDistanciasABares(listaRestaurantes.value!!, ubicacionUsuario!!) {
          hashMap: HashMap<String, Int?> ->
          _distancias.postValue(hashMap)
          Unit
        }
    }
  }

  fun ubicacionDisponible(ubicacionUsuario: Location?) {
    this.ubicacionUsuario = ubicacionUsuario
  }
}
