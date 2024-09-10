package com.barapp.viewModels.sharedViewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.CalificacionPromedio

class PantallaOpinionesSharedViewModel : ViewModel() {
    lateinit var idRestauranteSeleccionado: String
    lateinit var nombreRestauranteSeleccionado: String
    lateinit var caracteristicasRestauranteSeleccionado: Map<String, CalificacionPromedio>
    var puntuacionRestauranteSeleccionado: Double = 0.0
}