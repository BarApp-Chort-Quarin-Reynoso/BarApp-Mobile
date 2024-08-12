package com.barapp.viewModels.sharedViewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.Restaurante

class RestauranteSeleccionadoSharedViewModel : ViewModel() {
  var restaurante: Restaurante? = null
  var distancia: Int? = null

  fun cleanSelected() {
    restaurante = null
    distancia = null
  }
}
