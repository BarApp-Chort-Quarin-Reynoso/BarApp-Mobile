package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.Restaurante
import com.barapp.model.Ubicacion
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.util.stream.Collectors

class PantallaBaresCercanosViewModel : ViewModel() {
  private val restauranteRepository = RestauranteRepository.instance

  private val ubicacionUsuario: Ubicacion? = null
  private var region: LatLngBounds? = null

  private val _restaurantes = MutableLiveData<Set<Restaurante>>(HashSet())
  val restaurantes: LiveData<Set<Restaurante>> = _restaurantes
  
  private val _loading = MutableLiveData<Boolean>()
  val loading: LiveData<Boolean> = _loading

  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  var cameraPosition: CameraPosition? = null

  fun buscarBares(northeast: LatLng, southwest: LatLng) {
    _loading.value = true

    restauranteRepository.getRestaurantesPorArea(northeast, southwest, object : FirestoreCallback<List<Restaurante>> {
      override fun onSuccess(result: List<Restaurante>) {
        _loading.postValue(false)
        _restaurantes.postValue(addRestaurantes(result))
      }

      override fun onError(exception: Throwable) {
        _loading.postValue(false)
        _error.postValue(exception)
      }
    })
  }

  private fun addRestaurantes(nuevos: List<Restaurante>): Set<Restaurante> {
    val agregados: MutableSet<Restaurante> = mutableSetOf()
    restaurantes.value?.let { agregados.addAll(it) }
    agregados.addAll(nuevos)
    return agregados
  }
}
