package com.barapp.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.Restaurante
import com.barapp.util.Maps.Companion.calcularDistanciasABares
import java.util.Locale
import java.util.stream.Collectors

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
  private val _textoBusquedaIngresado = MutableLiveData<String>()
  val textoBusquedaIngresado: LiveData<String> = _textoBusquedaIngresado
  private val _orderedDescending = MutableLiveData<Boolean>()
  val orderedDescending: LiveData<Boolean> = _orderedDescending

  var minEstrellas: Int = 0

  fun buscarRestaurantesSegunTexto(textoBusqueda: String) {
      _textoBusquedaIngresado.postValue(textoBusqueda)

      restauranteRepository.buscarTodos(
        object : FirestoreCallback<List<Restaurante>> {
          override fun onSuccess(result: List<Restaurante>) {
            _listaRestaurantes.postValue(
              result
                .stream()
                .filter { restaurante ->
                  restaurante.nombre
                    .uppercase(Locale.getDefault())
                    .contains(textoBusqueda.uppercase(Locale.getDefault()))
                }
                .filter { restaurante ->
                  minEstrellas == 0 || restaurante.puntuacion > minEstrellas
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

  fun ordenarPorRating() {
    val listaRestaurantes = listaRestaurantes.value
    if (listaRestaurantes != null) {
        val listaOrdenada = if (orderedDescending.value == true) {
          listaRestaurantes.sortedBy { it.puntuacion }
        } else {
          listaRestaurantes.sortedByDescending { it.puntuacion }
        }
        _listaRestaurantes.postValue(listaOrdenada)
        _orderedDescending.postValue(orderedDescending.value != true)
    }
  }

  fun applyFilters() {
    buscarRestaurantesSegunTexto(textoBusquedaIngresado.value!!)
  }
}
