package com.barapp.viewModels.sharedViewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.Horario
import com.barapp.model.Restaurante
import java.time.LocalDate

class CrearReservaSharedViewModel : ViewModel() {
  lateinit var textoCantidadPersonas: String
  lateinit var textoFechaReserva: String

  var cantidadPersonas = 0
  lateinit var fechaReserva: LocalDate
  lateinit var horaReserva: Horario

  lateinit var barSeleccionado: Restaurante

  var vieneDePantallaConfirmacionReserva = false
  var indexChipSeleccionado = 0
  lateinit var listaHorariosDeshabilitados: ArrayList<Horario>
}
