package com.barapp.viewModels.sharedViewModels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.data.repositories.DetalleRestauranteRepository
import com.barapp.data.repositories.ReservaRepository
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Opinion
import com.barapp.model.Restaurante
import timber.log.Timber

class RestauranteSeleccionadoSharedViewModel : ViewModel() {
  var restaurante: Restaurante? = null
  var distancia: Int? = null

  private val restauranteRepository = RestauranteRepository.instance

  private val _restaurante: MutableLiveData<Restaurante> = MutableLiveData()
  val restauranteLd: LiveData<Restaurante> = _restaurante

  fun setSyncRestaurante(restaurante: Restaurante) {
    _restaurante.postValue(restaurante)
  }

  fun buscarRestaurante(idRestaurante: String) {
    restauranteRepository.buscarPorIdConDetalle(idRestaurante, object : FirestoreCallback<Restaurante> {
      override fun onSuccess(restaurante: Restaurante) {
        _restaurante.postValue(restaurante)
      }

      override fun onError(exception: Throwable) {
        Timber.e(exception)
      }
    })
  }
}
