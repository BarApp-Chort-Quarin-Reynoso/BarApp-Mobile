package com.barapp.viewModels

import androidx.lifecycle.*
import com.barapp.R
import com.barapp.data.utils.FirestoreCallback
import com.barapp.data.repositories.RestauranteRepository
import com.barapp.model.DetalleRestaurante
import com.barapp.model.Horario
import com.barapp.model.Restaurante
import com.barapp.model.TipoComida
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import timber.log.Timber
import java.time.format.DateTimeFormatter

class PantallaCrearReservaViewModel : ViewModel() {

  private val restauranteRepo: RestauranteRepository = RestauranteRepository.instance

  var indexChipSeleccionado = 0
  var todosChipsInhabilitadosDesayuno = true
  var todosChipsInhabilitadosAlmuerzo = true
  var todosChipsInhabilitadosMerienda = true
  var todosChipsInhabilitadosCena = true

  private var vieneDePantallaConfirmacionReserva = false

  lateinit var idUsuario: String

  lateinit var barSeleccionado: Restaurante
  var cantidadPersonas: Int = 0
  lateinit var fechaReserva: LocalDate
  lateinit var horaReserva: Horario
  private lateinit var ultimaHoraSeleccionadaTexto: String

  private val zonaArgentina = ZoneId.of("America/Buenos_Aires")
  private var diaSeleccionadoEnLong: Long = MaterialDatePicker.todayInUtcMilliseconds()

  private val _textoCantidadPersonas: MutableLiveData<String> = MutableLiveData()
  val textoCantidadPersonas: LiveData<String> = _textoCantidadPersonas

  private val _textoFechaReserva: MutableLiveData<String> = MutableLiveData()
  val textoFechaReserva: LiveData<String> = _textoFechaReserva

  private val _habilitarBotonAgregarPersona: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarBotonAgregarPersona: LiveData<Boolean> = _habilitarBotonAgregarPersona

  private val _habilitarBotonQuitarPersona: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarBotonQuitarPersona: LiveData<Boolean> = _habilitarBotonQuitarPersona

  private val _datePicker: MutableLiveData<MaterialDatePicker<Long>> = MutableLiveData()
  val datePicker: LiveData<MaterialDatePicker<Long>> = _datePicker

  private val _habilitarCardDatePicker: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarCardDatePicker: LiveData<Boolean> = _habilitarCardDatePicker

  private val _horariosDesayuno: MutableLiveData<List<Horario>> = MutableLiveData()
  val horariosDesayuno: LiveData<List<Horario>> = _horariosDesayuno

  private val _horariosAlmuerzo: MutableLiveData<List<Horario>> = MutableLiveData()
  val horariosAlmuerzo: LiveData<List<Horario>> = _horariosAlmuerzo

  private val _horariosMerienda: MutableLiveData<List<Horario>> = MutableLiveData()
  val horariosMerienda: LiveData<List<Horario>> = _horariosMerienda

  private val _horariosCena: MutableLiveData<List<Horario>> = MutableLiveData()
  val horariosCena: LiveData<List<Horario>> = _horariosCena

  private val _habilitarDialogTodosDeshabilitados: MutableLiveData<Boolean> = MutableLiveData()
  val habilitarDialogTodosDeshabilitados: LiveData<Boolean> = _habilitarDialogTodosDeshabilitados

  private val _horariosDiaSeleccionado: MutableLiveData<List<Horario>> = MutableLiveData()
  val horariosDiaSeleccionado: LiveData<List<Horario>> = _horariosDiaSeleccionado

  init {
    _habilitarBotonAgregarPersona.value = true
    _habilitarBotonQuitarPersona.value = true
    _horariosDesayuno.value = ArrayList()
    _horariosAlmuerzo.value = ArrayList()
    _horariosMerienda.value = ArrayList()
    _horariosCena.value = ArrayList()
    _horariosDiaSeleccionado.value = ArrayList()
  }

  /**
   * Si la cantidad actual de personas es igual a 15, se deshabilita el botón AgregarPersona. En
   * cualquier otro caso, se suma uno y se guarda en el atributo cantidadPersonas.
   *
   * @author Julio Chort
   */
  fun agregarPersona() {

    cantidadPersonas++

    if (cantidadPersonas == 15) {
      _textoCantidadPersonas.value = ("15 personas")
      _habilitarBotonAgregarPersona.value = false
    } else {
      _textoCantidadPersonas.value = ("$cantidadPersonas personas")
      _habilitarBotonQuitarPersona.value = true
    }
  }

  /**
   * Si la cantidad actual de personas es igual a 1, se deshabilita el botón QuitarPersona. En
   * cualquier otro caso, se resta uno y se guarda en el atributo cantidadPersonas.
   *
   * @author Julio Chort
   */
  fun quitarPersona() {

    cantidadPersonas--

    if (cantidadPersonas == 1) {
      _textoCantidadPersonas.value = ("1 persona")
      _habilitarBotonQuitarPersona.value = false
    } else {
      _textoCantidadPersonas.value = ("$cantidadPersonas personas")
      _habilitarBotonAgregarPersona.value = true
    }
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
   * Primero se crea un [CalendarConstraints] para asignarselo luego al [MaterialDatePicker], con
   * intención de que el usuario sólo pueda elegir una fecha entre el día de hoy y el mes siguiente.
   *
   * @author Julio Chort
   */
  fun crearDatePicker() {
    // Para que sólo pueda abrirse una vez
    this._habilitarCardDatePicker.postValue(false)

    val hoy = MaterialDatePicker.todayInUtcMilliseconds()
    val calendario = Calendar.getInstance(TimeZone.getTimeZone(zonaArgentina))

    calendario.timeInMillis = hoy
    calendario.add(Calendar.MONTH, 1)
    val unMesDespues = calendario.timeInMillis

    // Se construyen las restricciones del calendario
    val constraintsBuilder =
      CalendarConstraints.Builder()
        .setStart(hoy)
        .setEnd(unMesDespues)
        .setValidator(DateValidatorPointForward.now())

    // Se asignan tanto las restricciones como el resto de propiedades
    val datePicker =
      MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.pantalla_reservar_titulo_calendario)
        .setSelection(diaSeleccionadoEnLong)
        .setCalendarConstraints(constraintsBuilder.build())
        .setPositiveButtonText(R.string.boton_seleccionar)
        .build()

    datePicker.addOnPositiveButtonClickListener { selection ->
      diaSeleccionadoEnLong = selection
      fechaReserva =
        Date(
            selection -
              TimeZone.getTimeZone(zonaArgentina)
                .getOffset(MaterialDatePicker.todayInUtcMilliseconds())
          )
          .toInstant()
          .atZone(zonaArgentina)
          .toLocalDate()

      _textoFechaReserva.postValue(fechaAFormatoTexto(fechaReserva))

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val fechaCompleta = fechaReserva.format(formatter)

      val mesAnio = fechaCompleta.substring(0, 7)

      buscarHorarios(barSeleccionado.correo, mesAnio, fechaCompleta)
    }
    datePicker.addOnDismissListener { _habilitarCardDatePicker.postValue(true) }

    this._datePicker.postValue(datePicker)
  }

  /**
   * Se pasa la fecha de [LocalDate] a un formato en el sentido de [X, Y de Z], donde X son las
   * primeras 3 letras del día de la semana, Y es el número del día en el mes y Z es el mes del año.
   * El formato se encuentra disponible únicamente en español.
   *
   * @author Julio Chort
   */
  private fun fechaAFormatoTexto(fecha: LocalDate): String {

    return diaDeLaSemana(fecha.dayOfWeek.toString()) +
      " " +
      fecha.dayOfMonth +
      " de " +
      mesDelAnio(fecha.month.toString())
  }

  /**
   * Se crean 4 [ArrayList] de tipo [Horario] para los 4 [TipoComida] existentes. Luego se recorren
   * los horarios del [DetalleRestaurante] por [TipoComida] y se añade el horario a la lista
   * correspondiente. Por último se copian esas listas a las variables [MutableLiveData] generadas.
   *
   * @author Julio Chort
   */
  private fun mostrarHorariosDisponiblesParaEsaFecha() {
    val listaDesayuno = ArrayList<Horario>()
    val listaAlmuerzo = ArrayList<Horario>()
    val listaMerienda = ArrayList<Horario>()
    val listaCena = ArrayList<Horario>()

    for (horario in horariosDiaSeleccionado.value!!) {

      when (horario.tipoComida) {
        TipoComida.DESAYUNO -> listaDesayuno.add(horario)
        TipoComida.ALMUERZO -> listaAlmuerzo.add(horario)
        TipoComida.MERIENDA -> listaMerienda.add(horario)
        TipoComida.CENA -> listaCena.add(horario)
        else -> return
      }
    }

    _horariosDesayuno.value = listaDesayuno
    _horariosAlmuerzo.value = listaAlmuerzo
    _horariosMerienda.value = listaMerienda
    _horariosCena.value = listaCena

    comprobarSiSeMuestraDialogTodosInhabilitados()
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

    for (horario in horariosDiaSeleccionado.value!!) {

      if (horario.horario.substring(0,5) == ultimaHoraSeleccionadaTexto) {
        horaReserva = horario
      }
    }
  }

  private fun buscarHorarios(correo: String, mesAnio: String, diaSeleccionado: String) {
    restauranteRepo.buscarHorariosPorCorreo(correo, mesAnio, object : FirestoreCallback<Map<String,List<Horario>>> {
      override fun onSuccess(result: Map<String,List<Horario>>) {
        val horarios = result[diaSeleccionado]
        Timber.d("Horarios: $horarios")
        if (horarios.isNullOrEmpty()) {
          comprobarSiSeMuestraDialogTodosInhabilitados()
        }
        else {
          _horariosDiaSeleccionado.postValue(horarios!!)
          mostrarHorariosDisponiblesEnListado(horarios)
        }

      }

      override fun onError(exception: Throwable) {
        Timber.e(exception)
      }
    })
  }

  private fun mostrarHorariosDisponiblesEnListado(horarios: List<Horario>) {
    val listaDesayuno = ArrayList<Horario>()
    val listaAlmuerzo = ArrayList<Horario>()
    val listaMerienda = ArrayList<Horario>()
    val listaCena = ArrayList<Horario>()

    for (horario in horarios) {

      when (horario.tipoComida) {
        TipoComida.DESAYUNO -> listaDesayuno.add(horario)
        TipoComida.ALMUERZO -> listaAlmuerzo.add(horario)
        TipoComida.MERIENDA -> listaMerienda.add(horario)
        TipoComida.CENA -> listaCena.add(horario)
        else -> return
      }
    }

    _horariosDesayuno.value = listaDesayuno
    _horariosAlmuerzo.value = listaAlmuerzo
    _horariosMerienda.value = listaMerienda
    _horariosCena.value = listaCena

    Timber.d("Horarios desayuno: $listaDesayuno")
    Timber.d("Horarios almuerzo: $listaAlmuerzo")
    Timber.d("Horarios merienda: $listaMerienda")
    Timber.d("Horarios cena: $listaCena")
  }

  /**
   * Retorna la posición del [Horario] en la [listaHorariosAparicion]. En caso de no encontrarse en
   * la lista, devuelve el valor -1.
   *
   * @author Julio Chort
   */
  private fun posicionHorarioDentroDeLista(
    horarioReserva: Horario,
    listaHorariosAparicion: ArrayList<Pair<Horario, Int>>,
  ): Int {
    var result = -1
    var index = 0

    while ((index in 0 until listaHorariosAparicion.size) && (result == -1)) {
      if (listaHorariosAparicion[index].first.horario == horarioReserva.horario) {
        result = index
      }

      index++
    }

    return result
  }

  /**
   * Comprueba si el [Horario] pasado por argumento se encuentra dentro de la lista de horarios
   * deshabilitados. Este método fue creado debido a que el [contains] no era suficiente.
   *
   * @author Julio Chort
   */
  fun comprobarSiEsHorarioHabilitado(horario: Horario): Boolean {
    return false
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
      "OCTOBER" -> "Octubre"
      "NOVEMBER" -> "Noviembre"
      "DECEMBER" -> "Diciembre"
      else -> ""
    }
  }
}
