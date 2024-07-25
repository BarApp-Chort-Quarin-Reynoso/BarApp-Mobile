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
import timber.log.Timber

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

  private val _loadingCercaDeTi = MutableLiveData<Boolean>()
  val loadingCercaDeTi: LiveData<Boolean> = _loadingCercaDeTi

  private val _loadingDestacados = MutableLiveData<Boolean>()
  val loadingDestacados: LiveData<Boolean> = _loadingDestacados

  private val _loadingVistosRecientemente = MutableLiveData<Boolean>()
  val loadingVistosRecientemente: LiveData<Boolean> = _loadingVistosRecientemente

  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  private val disposables: MutableList<Disposable>

  init {
    disposables = ArrayList()
  }

  fun buscarRestaurantesDestacados() {
    _loadingDestacados.value = true
    restauranteRepository.buscarDestacados(
      object : FirestoreCallback<List<Restaurante>> {
        override fun onSuccess(result: List<Restaurante>) {
          _loadingDestacados.value = false
          _listaRestaurantesDestacados.value = result
        }

        override fun onError(exception: Throwable) {
          _loadingDestacados.value = false
          _error.value = exception
        }
      }
    )
  }

  fun buscarRestaurantesCercaDeTi() {
    _loadingCercaDeTi.value = true
    // Buscar restaurantes
    restauranteRepository.buscarTodos(
      object : FirestoreCallback<List<Restaurante>> {
        override fun onSuccess(result: List<Restaurante>) {
          _loadingCercaDeTi.value = false
          _listaRestaurantesCercaDeTi.postValue(result)
        }

        override fun onError(exception: Throwable) {
          _loadingCercaDeTi.value = false
          _error.postValue(exception)
        }
      }
    )
  }

  fun buscarRestaurantesVistosRecientemente() {
    _loadingVistosRecientemente.value = true
    restauranteVistoRecientementeRepository.buscarVistosRecientementeDelUsuario(
      usuario!!.id,
      object : FirestoreCallback<LinkedList<Restaurante>> {
        override fun onSuccess(result: LinkedList<Restaurante>) {
          _loadingVistosRecientemente.value = false
          _listaRestaurantesVistosRecientemente.postValue(result)
        }

        override fun onError(exception: Throwable) {
          _loadingVistosRecientemente.value = false
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
    Timber.e("calcularDistanciasCercaDeTi ejecutado")
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
