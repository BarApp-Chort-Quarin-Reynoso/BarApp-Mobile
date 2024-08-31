package com.barapp.viewModels

import androidx.lifecycle.*
import com.barapp.R
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Horario
import com.barapp.model.HorarioConCapacidadDisponible
import com.barapp.model.Restaurante
import com.barapp.model.TipoComida
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import timber.log.Timber
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import kotlin.collections.LinkedHashMap

class PantallaCrearReservaViewModel : ViewModel() {

  private val restauranteRepo: RestauranteRepository = RestauranteRepository.instance

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val formatterMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

  var indexChipSeleccionado = 0
  var todosChipsInhabilitadosDesayuno = true
  var todosChipsInhabilitadosAlmuerzo = true
  var todosChipsInhabilitadosMerienda = true
  var todosChipsInhabilitadosCena = true

  private var vieneDePantallaConfirmacionReserva = false

  lateinit var idUsuario: String

  lateinit var barSeleccionado: Restaurante
  var cantidadPersonas: Int = 0

  lateinit var horaReserva: Horario
  private lateinit var ultimaHoraSeleccionadaTexto: String

  val zonaArgentina: ZoneId = ZoneId.of("America/Buenos_Aires")
  private var diaSeleccionadoEnLong: Long = MaterialDatePicker.todayInUtcMilliseconds()

  private val _textoCantidadPersonas: MutableLiveData<String> = MutableLiveData()
  val textoCantidadPersonas: LiveData<String> = _textoCantidadPersonas

  private val _fechaReserva: MutableLiveData<LocalDate?> = MutableLiveData()
  val fechaReserva: LiveData<LocalDate?> = _fechaReserva

  private val _habilitarBotonAgregarPersona: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarBotonAgregarPersona: LiveData<Boolean> = _habilitarBotonAgregarPersona

  private val _habilitarBotonQuitarPersona: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarBotonQuitarPersona: LiveData<Boolean> = _habilitarBotonQuitarPersona

  private val _datePicker: MutableLiveData<MaterialDatePicker<Long>> = MutableLiveData()
  val datePicker: LiveData<MaterialDatePicker<Long>> = _datePicker

  private val _habilitarCardDatePicker: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarCardDatePicker: LiveData<Boolean> = _habilitarCardDatePicker

  private val _horariosDesayuno: MutableLiveData<Map<List<String>, Int>> = MutableLiveData()
  val horariosDesayuno: LiveData<Map<List<String>, Int>> = _horariosDesayuno

  private val _horariosAlmuerzo: MutableLiveData<Map<List<String>, Int>> = MutableLiveData()
  val horariosAlmuerzo: LiveData<Map<List<String>, Int>> = _horariosAlmuerzo

  private val _horariosMerienda: MutableLiveData<Map<List<String>, Int>> = MutableLiveData()
  val horariosMerienda: LiveData<Map<List<String>, Int>> = _horariosMerienda

  private val _horariosCena: MutableLiveData<Map<List<String>, Int>> = MutableLiveData()
  val horariosCena: LiveData<Map<List<String>, Int>> = _horariosCena

  private val _habilitarDialogTodosDeshabilitados: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarDialogTodosDeshabilitados: LiveData<Boolean> = _habilitarDialogTodosDeshabilitados

  private val _horariosDiaSeleccionado: MutableLiveData<List<HorarioConCapacidadDisponible>> =
    MutableLiveData()
  val horariosDiaSeleccionado: LiveData<List<HorarioConCapacidadDisponible>> =
    _horariosDiaSeleccionado

  private val _horariosPorMes: MutableLiveData<Map<LocalDate, Map<TipoComida, HorarioConCapacidadDisponible>>> =
    MutableLiveData()
  val horariosPorMes: LiveData<Map<LocalDate, Map<TipoComida, HorarioConCapacidadDisponible>>> =
    _horariosPorMes

  private val _claredChipGroup: MutableLiveData<Boolean> = MutableLiveData()
  val clearedChipGroup: LiveData<Boolean> = _claredChipGroup

  init {
    _habilitarBotonAgregarPersona.value = true
    _habilitarBotonQuitarPersona.value = false
    _horariosDesayuno.value = HashMap()
    _horariosAlmuerzo.value = HashMap()
    _horariosMerienda.value = HashMap()
    _horariosCena.value = HashMap()
    _horariosDiaSeleccionado.value = ArrayList()
    _claredChipGroup.value = false
  }

  /**
   * Si la cantidad actual de personas es igual a 15, se deshabilita el botón AgregarPersona. En
   * cualquier otro caso, se suma uno y se guarda en el atributo cantidadPersonas.
   *
   * @author Julio Chort
   */
  fun agregarPersona() {
    cantidadPersonas++
    _textoCantidadPersonas.value = ("$cantidadPersonas personas")
    _habilitarBotonQuitarPersona.value = true
    _habilitarBotonAgregarPersona.value = (cantidadPersonas != obtenerMaximoPersonas())
    actualizarEstadoChips()
  }

  /**
   * Si la cantidad actual de personas es igual a 1, se deshabilita el botón QuitarPersona. En
   * cualquier otro caso, se resta uno y se guarda en el atributo cantidadPersonas.
   *
   * @author Julio Chort
   */
  fun quitarPersona() {

    cantidadPersonas--
    _textoCantidadPersonas.value =
      if (cantidadPersonas == 1) "1 persona" else "$cantidadPersonas personas"
    _habilitarBotonQuitarPersona.value = (cantidadPersonas > 1)
    _habilitarBotonAgregarPersona.value = true
    actualizarEstadoChips()
  }

  /**
   * Si el número es de una sola cifra [1..9] el segundo caracter del texto será un espacio ' ',
   * entonces el .isDigit() lanzará false. En caso de un número de dos cifras lanzará true. La
   * función se ejecuta una única vez, cuando se crea el fragmento. De esta forma si se cambia el
   * [R.string.EJEMPLO_pantalla_crear_reserva_texto_cantidad_personas] no se debe cambiar ninguna
   * otra parte del código.
   *
   * @author Julio Chort
   */
  fun cantidadDePersonasDeTextoANumero(cantidadPersonasTexto: String) {
    this._textoCantidadPersonas.value = cantidadPersonasTexto

    cantidadPersonas =
      if (cantidadPersonasTexto[1].isDigit()) {
        cantidadPersonasTexto.substring(0, 2).toInt()
      } else {
        cantidadPersonasTexto[0].digitToInt()
      }
  }

  /**
   * Se pasa la fecha de [LocalDate] a un formato en el sentido de [X, Y de Z], donde X son las
   * primeras 3 letras del día de la semana, Y es el número del día en el mes y Z es el mes del año.
   * El formato se encuentra disponible únicamente en español.
   *
   * @author Julio Chort
   */
  fun fechaAFormatoTexto(fecha: LocalDate): String {
    return diaDeLaSemana(fecha.dayOfWeek.toString()) +
        " " +
        fecha.dayOfMonth +
        " de " +
        mesDelAnio(fecha.month.toString())
  }

  /**
   * Si el [Chip] fue pulsado puede ser que sea para seleccionarlo o deseleccionarlo. En el primer
   * caso, se convierte en el [ultimoChipSeleccionado] y el [MutableLiveData] para hablitar el botón
   * crearReserva pasa a valor True. En el segundo caso, el [MutableLiveData] toma el valor
   * False por lo que no se puede seguir con la reserva.
   *
   * @author Julio Chort
   */
  fun datosDelChipSeleccionado(textoChipSeleccionado: String, indexChip: Int) {

    ultimaHoraSeleccionadaTexto = textoChipSeleccionado
    indexChipSeleccionado = indexChip
  }

  /**
   * Se itera en la lista de horarios del [DetalleRestaurante] del bar seleccionado buscando el que
   * coincida con el texto de la [ultimaHoraSeleccionadaTexto]. El que coincida es el horario
   * seleccionado por el usuario.
   *
   * @author Julio Chort
   */
  fun pasarChipSeleccionadoAHorario() {

    for (horarioConCapacidad in horariosDiaSeleccionado.value!!) {
      for (horario in horarioConCapacidad.horarios) {
        if (horario.substring(0, 5) == ultimaHoraSeleccionadaTexto) {
          horaReserva = Horario(horario, horarioConCapacidad.tipoComida)
        }
      }
    }
  }

  fun buscarHorariosPorMes(mesAnio: YearMonth) {
    if (horariosPorMes.value.isNullOrEmpty()) {
      val correo = barSeleccionado.correo
      val mesAnioString = mesAnio.format(formatterMonth)
      restauranteRepo.buscarHorariosPorCorreo(
        correo,
        mesAnioString,
        3,
        object : FirestoreCallback<Map<String, Map<String, HorarioConCapacidadDisponible>>> {
          override fun onSuccess(result: Map<String, Map<String, HorarioConCapacidadDisponible>>) {
            _horariosPorMes.postValue(mapHorarios(result))
          }

          override fun onError(exception: Throwable) {
            Timber.e(exception)
          }
        })
    } else {
      _horariosPorMes.postValue(horariosPorMes.value)
    }
  }

  fun setFechaSeleccionada(diaSeleccionado: LocalDate?) {
    _fechaReserva.postValue(diaSeleccionado)

    if (diaSeleccionado == null) {
      _horariosDiaSeleccionado.postValue(ArrayList())
    } else {
      val horarios = (horariosPorMes.value?.get(diaSeleccionado)?.values ?: ArrayList())
        .stream()
        .collect(Collectors.toList())
      if (!horarios.isNullOrEmpty()) {
        Timber.d("Horarios: $horarios")
        _horariosDiaSeleccionado.postValue(horarios)
        mostrarHorariosDisponiblesEnListado(horarios)
      }
    }
  }

  private fun obtenerMaximoPersonas(): Int {
    return 30
  }

  private fun cantidadMaximaPersonas(horario: HorarioConCapacidadDisponible): Int {
    var cantidadPersonasMaxima = 1
    for (mesa in horario.mesas) {
      if (mesa.cantidadDePersonasPorMesa > cantidadPersonasMaxima) {
        cantidadPersonasMaxima = mesa.cantidadDePersonasPorMesa
      }
    }
    return cantidadPersonasMaxima
  }

  private fun mostrarHorariosDisponiblesEnListado(horarios: List<HorarioConCapacidadDisponible>) {
    for (horario in horarios) {
      val cantidadMaximaPersonas = cantidadMaximaPersonas(horario)
      val tipoComidaHorario = horario.tipoComida
      when (tipoComidaHorario) {
        TipoComida.DESAYUNO -> _horariosDesayuno.value =
          mapOf(horario.horarios to cantidadMaximaPersonas)

        TipoComida.ALMUERZO -> _horariosAlmuerzo.value =
          mapOf(horario.horarios to cantidadMaximaPersonas)

        TipoComida.MERIENDA -> _horariosMerienda.value =
          mapOf(horario.horarios to cantidadMaximaPersonas)

        TipoComida.CENA -> _horariosCena.value = mapOf(horario.horarios to cantidadMaximaPersonas)
        else -> return
      }
    }

    Timber.d("Horarios desayuno: ${_horariosDesayuno.value}")
    Timber.d("Horarios almuerzo: ${_horariosAlmuerzo.value}")
    Timber.d("Horarios merienda: ${_horariosMerienda.value}")
    Timber.d("Horarios cena: ${_horariosCena.value}")
  }

  private fun actualizarEstadoChips() {
    if (_horariosDiaSeleccionado.value.isNullOrEmpty())
      return

    val horarios = _horariosDiaSeleccionado.value!!
    _claredChipGroup.value = true
    for (horario in horarios) {
      val cantidadMaximaPersonas = cantidadMaximaPersonas(horario)
      val tipoComidaHorario = horario.tipoComida
      when (tipoComidaHorario) {
        TipoComida.DESAYUNO -> _horariosDesayuno.value =
          mapOf(horario.horarios to cantidadMaximaPersonas)

        TipoComida.ALMUERZO -> _horariosAlmuerzo.value =
          mapOf(horario.horarios to cantidadMaximaPersonas)

        TipoComida.MERIENDA -> _horariosMerienda.value =
          mapOf(horario.horarios to cantidadMaximaPersonas)

        TipoComida.CENA -> _horariosCena.value = mapOf(horario.horarios to cantidadMaximaPersonas)
        else -> return
      }
    }

  }

  fun chipGroupCleared() {
    _claredChipGroup.value = false
  }

  /**
   * Comprueba si el [Horario] pasado por argumento se encuentra dentro de la lista de horarios
   * deshabilitados. Este método fue creado debido a que el [contains] no era suficiente.
   *
   * @author Julio Chort
   */
  fun esHorarioHabilitado(maxPersonas: Int): Boolean {
    return this.cantidadPersonas <= maxPersonas
  }

  /**
   * Cuando se regresa de la pantalla [PantallaConfirmacionReserva] se debe restablecer el
   * [ChipGroup] tal cual se había dejado, con los mismos horarios deshabilitados.
   *
   * @author Julio Chort
   */
  fun restablecerHorariosComoEstaban() {
    mostrarHorariosDisponiblesEnListado(horariosDiaSeleccionado.value!!)
  }

  /**
   * Si todos los [Chip] están deshabilitados, entonces se debe mostrar un [Dialog] informando al
   * usuario de la situación. Se chequea primero si no viene de [PantallaConfirmacionReserva],
   * debido a que de ser así hay al menos 1 [Chip] seleccionado, por lo que aunque no se sepa en qué
   * [TipoComida] está se sabe que no están todos deshabilitados.
   *
   * @author Julio Chort
   */
  private fun comprobarSiSeMuestraDialogTodosInhabilitados() {
    if (
      !vieneDePantallaConfirmacionReserva &&
      todosChipsInhabilitadosDesayuno &&
      todosChipsInhabilitadosAlmuerzo &&
      todosChipsInhabilitadosMerienda &&
      todosChipsInhabilitadosCena
    ) {
      _habilitarDialogTodosDeshabilitados.value = true
    }

    vieneDePantallaConfirmacionReserva = false
  }

  private fun mapHorarios(horarios: Map<String, Map<String, HorarioConCapacidadDisponible>>): Map<LocalDate, Map<TipoComida, HorarioConCapacidadDisponible>> {
    val map: MutableMap<LocalDate, MutableMap<TipoComida, HorarioConCapacidadDisponible>> =
      LinkedHashMap()
    horarios.forEach { (fecha, submap) ->
      val fechaConvertida: LocalDate = LocalDate.parse(fecha, formatter)
      map[fechaConvertida] = LinkedHashMap()
      submap.forEach { (tipoComida, horario) ->
        val tipoComidaConvertido = TipoComida.valueOf(tipoComida)
        map[fechaConvertida]!![tipoComidaConvertido] = horario
      }
    }

    return map
  }

  /**
   * Se traduce el día de la semana del inglés al español.
   *
   * @author Julio Chort
   */
  private fun diaDeLaSemana(enIngles: String): String {
    return when (enIngles) {
      "MONDAY" -> "Lun,"
      "TUESDAY" -> "Mar,"
      "WEDNESDAY" -> "Mie,"
      "THURSDAY" -> "Jue,"
      "FRIDAY" -> "Vie,"
      "SATURDAY" -> "Sab,"
      "SUNDAY" -> "Dom,"
      else -> ""
    }
  }

  /**
   * Se traduce el mes del inglés al español.
   *
   * @author Julio Chort
   */
  private fun mesDelAnio(enIngles: String): String {
    return when (enIngles) {
      "JANUARY" -> "Enero"
      "FEBRUARY" -> "Febrero"
      "MARCH" -> "Marzo"
      "APRIL" -> "Abril"
      "MAY" -> "Mayo"
      "JUNE" -> "Junio"
      "JULY" -> "Julio"
      "AUGUST" -> "Agosto"
      "SEPTEMBER" -> "Septiembre"
      "OCTOBER" -> "Octubre"
      "NOVEMBER" -> "Noviembre"
      "DECEMBER" -> "Diciembre"
      else -> ""
    }
  }
}
