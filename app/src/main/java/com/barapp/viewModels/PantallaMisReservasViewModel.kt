package com.barapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barapp.barapp.model.Reserva
import com.barapp.data.repositories.ReservaRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.model.EstadoReserva
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import timber.log.Timber

class PantallaMisReservasViewModel : ViewModel() {

  private val reservaRepo: ReservaRepository = ReservaRepository.instance
  lateinit var idUsuario: String

  private val _reservasPendientes = MutableLiveData<List<Reserva>>()
  val reservasPendientes: LiveData<List<Reserva>> = _reservasPendientes

  private val _reservasPasadas = MutableLiveData<List<Reserva>>()
  val reservasPasadas: LiveData<List<Reserva>> = _reservasPasadas

  private val _habilitarListaVacia = MutableLiveData<Boolean>()
  val habilitarListaVacia: LiveData<Boolean> = _habilitarListaVacia

  private val _loading = MutableLiveData<Boolean>()
  val loading: LiveData<Boolean> = _loading

  private val _error = MutableLiveData<Throwable>()
  val error: LiveData<Throwable> = _error

  init {
    _loading.value = true
  }

  fun buscarReservas() {
    _loading.value = true

    reservaRepo.buscarTodosAsociadosAUsuario(
      idUsuario,
      object : FirestoreCallback<List<Reserva>> {
        override fun onSuccess(result: List<Reserva>) {
          _loading.postValue(false)
          // Si la lista de reservas está vacia, habilitar el "disenio lista vacia"
          _habilitarListaVacia.value = result.isEmpty()
          separarPasadasYPendientes(result)
        }

        override fun onError(exception: Throwable) {
          Timber.e(exception)
          _loading.postValue(false)
        }
      },
    )
  }

  /** @author Julio Chort */
  private fun separarPasadasYPendientes(result: List<Reserva>) {

    val pendientes = ArrayList<Reserva>()
    val pasadas = ArrayList<Reserva>()

    for (reserva in result) {

      if (esReservaPasada(reserva)) {
        pasadas.add(reserva)
      } else {
        pendientes.add(reserva)
      }
    }

    val reversed = pasadas.reversed()

    _reservasPendientes.postValue(pendientes)
    _reservasPasadas.postValue(reversed)
  }

  /**
   * Primero chequeo el estado. Si es 'PENDIENTE', verifico el siguiente texto:
   * En el primer [if] se evalúa si la fecha es anterior al día de hoy. Si se cumple quiere decir
   * que la reserva es pasada. Luego, si no se cumple, la fecha de la reserva puede ser de hoy
   * (primer [else_if]). Si es hoy, hacemos la misma validación con la hora. Si es antes de la
   * actual entonces es pasada, y si la hora coincide chequeamos los minutos. Hay que tener en
   * cuenta que una reserva se considera pasada luego de 30 minutos del horario especificado en la
   * misma.
   *
   * @author Julio Chort
   */
  private fun esReservaPasada(reserva: Reserva): Boolean {

    if (reserva.estado != EstadoReserva.PENDIENTE) {
      return true
    }

    var result = false
    val diaActualArgentina = LocalDate.now(ZoneId.of("America/Buenos_Aires"))
    val horaActualArgentina = LocalTime.now(ZoneId.of("America/Buenos_Aires"))

    if (reserva.getFechaAsLocalDate().isBefore(diaActualArgentina)) {
      result = true
    } else if (reserva.getFechaAsLocalDate().isEqual(diaActualArgentina)) {
      if (reserva.horario.getHorarioAsLocalTime().isBefore(horaActualArgentina.minusMinutes(30))) {
        result = true
      }
    }

    return result
  }
}
