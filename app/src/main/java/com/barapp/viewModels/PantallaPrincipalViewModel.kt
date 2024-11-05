package com.barapp.viewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.data.repositories.RestauranteVistoRecientementeRepository
import com.barapp.util.Maps.Companion.calcularDistanciasABares
import io.reactivex.rxjava3.disposables.Disposable
import java.util.LinkedList
import java.util.stream.Collectors

class PantallaPrincipalViewModel(
  private val restauranteRepository: RestauranteRepository,
  private val restauranteVistoRecientementeRepository: RestauranteVistoRecientementeRepository,
) : ViewModel() {
  private var ubicacionUsuario: Location? = null
  var usuario: Usuario? = null
    set(value) {
      if (field == null) {
        field = value
      }
      field = value
    }

  private val _listaRestaurantesCercaDeTi = MutableLiveData<List<Restaurante>>()
  val listaRestaurantesCercaDeTi: LiveData<List<Restaurante>> = _listaRestaurantesCercaDeTi

  private val _listaRestaurantesFiltradaCercaDeTi = MutableLiveData<List<Restaurante>>()
  val listaRestaurantesFiltradaCercaDeTi: LiveData<List<Restaurante>> = _listaRestaurantesFiltradaCercaDeTi


  private val _listaRestaurantesDestacados = MutableLiveData<List<Restaurante>>()
  val listaRestaurantesDestacados: LiveData<List<Restaurante>> = _listaRestaurantesDestacados

  private val _listaRestaurantesVistosRecientemente = MutableLiveData<List<Restaurante>>()
  val listaRestaurantesVistosRecientemente: LiveData<List<Restaurante>> =
    _listaRestaurantesVistosRecientemente

  private val _distanciasCercaDeTi = MutableLiveData<HashMap<String, Int?>>(LinkedHashMap())
  val distanciasCercaDeTi: LiveData<HashMap<String, Int?>> = _distanciasCercaDeTi

  private val _distanciasDestacados = MutableLiveData<HashMap<String, Int?>>(LinkedHashMap())
  val distanciasDestacados: LiveData<HashMap<String, Int?>> = _distanciasDestacados

  private val _distanciasVistosRecientemente =
    MutableLiveData<HashMap<String, Int?>>(LinkedHashMap())
  val distanciasVistosRecientemente: LiveData<HashMap<String, Int?>> =
    _distanciasVistosRecientemente

  private val _loading = MutableLiveData<Boolean>()
  val loading: LiveData<Boolean> = _loading

  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  private val disposables: MutableList<Disposable>

  init {
    _loading.value = true
    disposables = ArrayList()
  }

  fun buscarRestaurantesDestacados() {
    restauranteRepository.buscarDestacados(
      object : FirestoreCallback<List<Restaurante>> {
        override fun onSuccess(result: List<Restaurante>) {
          _loading.value = false
          _listaRestaurantesDestacados.value = result
        }

        override fun onError(exception: Throwable) {
          _loading.value = false
          _error.value = exception
        }
      }
    )
  }

  fun buscarRestaurantesCercaDeTi() {
    // Buscar restaurantes
    restauranteRepository.buscarTodos(
      object : FirestoreCallback<List<Restaurante>> {
        override fun onSuccess(result: List<Restaurante>) {
          _loading.value = false
          _listaRestaurantesCercaDeTi.postValue(result)
        }

        override fun onError(exception: Throwable) {
          _loading.value = false
          _error.postValue(exception)
        }
      }
    )
  }

  fun buscarRestaurantesVistosRecientemente() {
    restauranteVistoRecientementeRepository.buscarVistosRecientementeDelUsuario(
      usuario!!.id,
      object : FirestoreCallback<LinkedList<Restaurante>> {
        override fun onSuccess(result: LinkedList<Restaurante>) {
          _loading.value = false
          _listaRestaurantesVistosRecientemente.postValue(result)
        }

        override fun onError(exception: Throwable) {
          _loading.value = false
          _error.postValue(exception)
        }
      },
    )
  }

  /**
   * Este metodo calcula la distancia desde el usuario a todos los bares de
   * *listaRestaurantesCercaDeTi*. Si la distancia calculada supera el valor de
   * *distanciaMaximaAMostrar*, se setea su valor en null.
   *
   * Al finalizar, se setean los valores en el [LiveData] *distanciasCercaDeTi*
   *
   * @author Federico Quarin
   */
  fun calcularDistanciasCercaDeTi() {
    if (listaRestaurantesCercaDeTi.value != null && ubicacionUsuario != null) {
      val subscription =
        calcularDistanciasABares(listaRestaurantesCercaDeTi.value!!, ubicacionUsuario!!) {
          hashMap: HashMap<String, Int?> ->
          _distanciasCercaDeTi.postValue(hashMap)
          Unit
        }
      disposables.add(subscription)
    }
  }

  fun filterRestaurantesCercaDeTi() {
    val distancias = distanciasCercaDeTi.value
    val listaRestaurantes = listaRestaurantesCercaDeTi.value
    if (distancias != null && listaRestaurantes != null) {
      val result = listaRestaurantes
        .stream()
        .filter { restaurante ->
          distancias[restaurante.id] != null
        }
        .collect(Collectors.toList())
      _listaRestaurantesFiltradaCercaDeTi.postValue(result)
    }
  }

  /**
   * Este metodo calcula la distancia desde el usuario a todos los bares de
   * *listaRestaurantesDestacados*. Si la distancia calculada supera el valor de
   * *distanciaMaximaAMostrar*, se setea su valor en null.
   *
   * Al finalizar, se setean los valores en el [LiveData] *distanciasCercaDeTi*
   *
   * @author Federico Quarin
   */
  fun calcularDistanciasDestacados() {
    if (listaRestaurantesDestacados.value != null && ubicacionUsuario != null) {
      val subscription =
        calcularDistanciasABares(listaRestaurantesDestacados.value!!, ubicacionUsuario!!) {
          hashMap: HashMap<String, Int?> ->
          _distanciasDestacados.postValue(hashMap)
          Unit
        }
      disposables.add(subscription)
    }
  }

  /**
   * Este metodo calcula la distancia desde el usuario a todos los bares de
   * *listaRestaurantesVistosRecientemente*. Si la distancia calculada supera el valor de
   * *distanciaMaximaAMostrar*, se setea su valor en null.
   *
   * Al finalizar, se setean los valores en el [LiveData] *distanciasCercaDeTi*
   *
   * @author Federico Quarin
   */
  fun calcularDistanciasVistosRecientemente() {
    if (listaRestaurantesVistosRecientemente.value != null && ubicacionUsuario != null) {
      val subscription =
        calcularDistanciasABares(
          listaRestaurantesVistosRecientemente.value!!,
          ubicacionUsuario!!,
        ) { hashMap: HashMap<String, Int?> ->
          _distanciasVistosRecientemente.postValue(hashMap)
          Unit
        }
      disposables.add(subscription)
    }
  }

  fun ubicacionDisponible(ubicacionUsuario: Location?) {
    this.ubicacionUsuario = ubicacionUsuario
  }

  override fun onCleared() {
    super.onCleared()
    for (d in disposables) {
      if (!d.isDisposed) d.dispose()
    }
  }

  class Factory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(PantallaPrincipalViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return PantallaPrincipalViewModel(
          RestauranteRepository.instance,
          RestauranteVistoRecientementeRepository.instance,
        )
          as T
      }
      throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
    }
  }
}
