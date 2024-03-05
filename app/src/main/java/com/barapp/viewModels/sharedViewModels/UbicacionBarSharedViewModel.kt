package com.barapp.viewModels.sharedViewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.Ubicacion

class UbicacionBarSharedViewModel : ViewModel() {

  lateinit var ubicacionRestaurante: Ubicacion
  lateinit var nombreRestaurante: String
  lateinit var logo: String
}
