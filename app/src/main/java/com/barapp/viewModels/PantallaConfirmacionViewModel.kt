package com.barapp.viewModels

import androidx.lifecycle.ViewModel
import com.barapp.model.Horario
import com.barapp.barapp.model.Reserva
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.data.repositories.ReservaRepository
import java.time.LocalDate

class PantallaConfirmacionViewModel : ViewModel() {

  private val reservaRepository = ReservaRepository.instance
  lateinit var barSeleccionado: Restaurante
  lateinit var usuario: Usuario

  fun crearReserva(cantPersonas: Int, fechaReserva: LocalDate, horaReserva: Horario): Reserva {
    val reserva = Reserva(cantPersonas, fechaReserva.toString(), barSeleccionado, horaReserva, usuario)

    reservaRepository.guardar(reserva)

    return reserva
  }
}
