package com.barapp.viewModels.sharedViewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.Restaurante

class RestauranteSeleccionadoSharedViewModel : ViewModel() {
  lateinit var restaurante: Restaurante
  var distancia: Int? = null
}
