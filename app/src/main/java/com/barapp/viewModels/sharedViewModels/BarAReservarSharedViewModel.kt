package com.barapp.viewModels.sharedViewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Restaurante

class BarAReservarSharedViewModel : ViewModel() {

  lateinit var barSeleccionado: Restaurante
  lateinit var detalleBarSeleccionado: DetalleRestaurante
}
