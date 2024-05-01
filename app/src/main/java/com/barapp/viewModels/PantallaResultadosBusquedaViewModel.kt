package com.barapp.viewModels

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.model.Restaurante
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.util.Maps.Companion.calcularDistanciasABares
import com.barapp.util.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.util.stream.Collectors
import timber.log.Timber

class PantallaResultadosBusquedaViewModel : ViewModel() {
  private val restauranteRepository: RestauranteRepository = RestauranteRepository.instance

  private var ubicacionUsuario: Location? = null
  private val _listaRestaurantes = MutableLiveData<List<Restaurante>>()
  val listaRestaurantes: LiveData<List<Restaurante>> = _listaRestaurantes
  private val _listaRestaurantesCompleta = MutableLiveData<List<Restaurante>>()
  val listaRestaurantesCompleta: LiveData<List<Restaurante>> = _listaRestaurantesCompleta
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


//      val queryParams = mapOf("nombre" to textoBusqueda)

//    restauranteRepository.buscarTodos
//      RetrofitInstance.restaurantApiService.getAllRestaurants(emptyMap()).enqueue(object : Callback<List<Restaurante>> {
//        override fun onResponse(call: Call<List<Restaurante>>, response: Response<List<Restaurante>>) {
//          if (response.isSuccessful) {
//            val data = response.body()
//            Timber.d("Data received: $data")
//
//            // Filter the list of restaurants based on the textoBusqueda string
//            val filteredData = data?.filter { restaurante ->
//              restaurante.nombre.uppercase(Locale.getDefault()).contains(textoBusqueda.uppercase(Locale.getDefault()))
//            }
//
//            // Post the filtered list to _listaRestaurantes
//            _listaRestaurantes.postValue(filteredData!!)
//          } else {
//            // Handle the error
//            Timber.e("Error: ${response.errorBody()}")
//          }
//        }
//
//        override fun onFailure(call: Call<List<Restaurante>>, t: Throwable) {
//          // Handle the failure
//          Timber.e(t, "Failure")
//        }
//      })
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

  fun ordenarPorDistancia() {
    val listaRestaurantes = listaRestaurantes.value
    val distancias = distancias.value
    if (listaRestaurantes != null && distancias != null) {
      val listaOrdenada = listaRestaurantes.sortedBy { distancias[it.id] }
      _listaRestaurantes.postValue(listaOrdenada)
    }
  }

  fun ordenarPorRating() {
    val listaRestaurantes = listaRestaurantes.value
    if (listaRestaurantes != null) {
      val listaOrdenada = listaRestaurantes.sortedByDescending { it.puntuacion }
      _listaRestaurantes.postValue(listaOrdenada)
    }
  }

  fun applyFilters() {
    // log "ApplyFilters"
    System.out.println("ApplyFilters")

    val listaRestaurantesCompleta = listaRestaurantesCompleta.value
    if (listaRestaurantesCompleta != null) {
      val listaFiltrada = listaRestaurantesCompleta
        .filter { restaurante: Restaurante ->
          restaurante.puntuacion > 4 // TODO: Use selected chip
        }
      _listaRestaurantes.postValue(listaFiltrada)
    }
  }

  fun resetFilters() {
    System.out.println("RestFilters")

    val listaRestaurantesCompleta = listaRestaurantesCompleta.value
    if (listaRestaurantesCompleta != null) {
      _listaRestaurantes.postValue(listaRestaurantesCompleta!!)
    }

  }

}
