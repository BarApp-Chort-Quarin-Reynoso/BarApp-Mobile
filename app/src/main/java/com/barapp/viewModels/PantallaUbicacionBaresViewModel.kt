package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barapp.model.Restaurante
import com.barapp.model.Ubicacion
import com.barapp.viewModels.sharedViewModels.UbicacionBarSharedViewModel
import com.google.android.gms.maps.model.LatLngBounds
import timber.log.Timber

class PantallaUbicacionBaresViewModel : ViewModel() {
  private val ubicacionUsuario: Ubicacion? = null
  private var region: LatLngBounds? = null
  private val _restaurantes = MutableLiveData<List<Restaurante>>()
  val restaurantes: LiveData<List<Restaurante>> = _restaurantes

  private fun buscarBaresCercanos() {
    Timber.w("Buscar bares cercanos no implementado")
  }

  /** Permite setear la region en la que se deben buscar los bares */
  fun setRegion(region: LatLngBounds?) {
    this.region = region
  }

  class Factory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(UbicacionBarSharedViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST") return PantallaUbicacionBaresViewModel() as T
      }
      throw IllegalArgumentException("UNKNOWN VIEW MODEL CLASS")
    }
  }
}
