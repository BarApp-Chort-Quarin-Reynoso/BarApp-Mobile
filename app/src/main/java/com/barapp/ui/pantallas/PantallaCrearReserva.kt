package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import com.barapp.R
import com.barapp.databinding.FragmentPantallaCrearReservaBinding
import com.barapp.databinding.ItemChipHorarioBinding
import com.barapp.databinding.ItemTituloHorarioBinding
import com.barapp.model.Horario
import com.barapp.model.HorarioConCapacidadDisponible
import com.barapp.model.TipoComida
import com.barapp.util.AvailableDatesValidator
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaCrearReservaViewModel
import com.barapp.viewModels.sharedViewModels.BarAReservarSharedViewModel
import com.barapp.viewModels.sharedViewModels.CrearReservaSharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Esta clase es un [Fragment] que se utiliza para que el usuario pueda crear la reserva en el
 * restaurante seleccionado.
 *
 * Esta pantalla necesita que el usuario cargue la cantidad de personas que asistirán a la reserva,
 * el día de la misma y el horario. Por último, la posibilidad de crear la reserva o cancelar e ir a
 * la pantalla anterior.
 *
 * @author Julio Chort
 */
class PantallaCrearReserva : Fragment() {

  private lateinit var binding: FragmentPantallaCrearReservaBinding

  private val viewModel: PantallaCrearReservaViewModel by viewModels()

  private val sharedViewModelProximaPantalla: CrearReservaSharedViewModel by
  navGraphViewModels(R.id.pantallaCrearReserva)

  private val sharedViewModelBarAReservar: BarAReservarSharedViewModel by
  navGraphViewModels(R.id.pantallaNavegacionPrincipal)

  private val activitySharedViewModel: MainActivityViewModel by activityViewModels()

  private var diaSeleccionadoEnLong: Long = MaterialDatePicker.todayInUtcMilliseconds()

  private var datePicker: MaterialDatePicker<Long>? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaCrearReservaBinding.inflate(inflater, container, false)

    // Se setea la transicion
    sharedElementEnterTransition = MaterialContainerTransform()

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Se copia el detalle restaurante para determinar los horarios disponibles
    viewModel.barSeleccionado = sharedViewModelBarAReservar.barSeleccionado

    // Se copia el id usuario para quitar los horarios ya reservados
    viewModel.idUsuario = activitySharedViewModel.usuario.value!!.id

    relacionarFragmentoConViewModel()

    binding.labelNombreRestaurante.text = sharedViewModelBarAReservar.barSeleccionado.nombre

    binding.toolbar.setNavigationOnClickListener { volverAtras() }

    binding.botonAgregarPersona.setOnClickListener {
      viewModel.agregarPersona()
      binding.botonCrearReserva.isEnabled = false
    }

    binding.botonQuitarPersona.setOnClickListener {
      viewModel.quitarPersona()
      binding.botonCrearReserva.isEnabled = false
    }

    binding.textInputFechaReserva.setOnClickListener {
      viewModel.buscarHorariosPorMes(YearMonth.now())
      limpiarDatosViejosHorarios()
    }

    viewModel.horariosPorMes.observe(viewLifecycleOwner) { h ->
      crearDatePicker(h)
    }

    binding.botonCrearReserva.isEnabled = false

    binding.botonCrearReserva.setOnClickListener {
      viewModel.pasarChipSeleccionadoAHorario()
      navegarAConfirmarReserva()
    }
  }

  private fun crearDatePicker(horariosPorMes: Map<LocalDate, Map<TipoComida, HorarioConCapacidadDisponible>>) {
    val hoy = MaterialDatePicker.todayInUtcMilliseconds()
    val calendario = Calendar.getInstance(TimeZone.getTimeZone(viewModel.zonaArgentina))

    calendario.timeInMillis = hoy
    calendario.add(Calendar.MONTH, 1)
    val unMesDespues = calendario.timeInMillis

    val availableDatesValidator = AvailableDatesValidator(horariosPorMes.keys)

    // Se construyen las restricciones del calendario
    val constraintsBuilder =
      CalendarConstraints.Builder()
        .setStart(hoy)
        .setEnd(unMesDespues)
        .setValidator(DateValidatorPointForward.now())
        .setValidator(availableDatesValidator)
        .setFirstDayOfWeek(1)

    // Se asignan tanto las restricciones como el resto de propiedades
    datePicker = MaterialDatePicker.Builder.datePicker()
      .setTitleText(R.string.pantalla_reservar_titulo_calendario)
      .setSelection(diaSeleccionadoEnLong)
      .setCalendarConstraints(constraintsBuilder.build())
      .setPositiveButtonText(R.string.boton_seleccionar)
      .build()

    datePicker!!.addOnPositiveButtonClickListener { selection ->
      diaSeleccionadoEnLong = selection
      val fechaReserva =
        Date(
          selection -
              TimeZone.getTimeZone(viewModel.zonaArgentina)
                .getOffset(MaterialDatePicker.todayInUtcMilliseconds())
        )
          .toInstant()
          .atZone(viewModel.zonaArgentina)
          .toLocalDate()

      viewModel.setFechaSeleccionada(fechaReserva)
    }

    datePicker!!.addOnCancelListener {
      viewModel.setFechaSeleccionada(null)
    }

    datePicker!!.addOnNegativeButtonClickListener {
      viewModel.setFechaSeleccionada(null)
    }

    if (!sharedViewModelProximaPantalla.vieneDePantallaConfirmacionReserva) {
      datePicker!!.show(parentFragmentManager, "DATE_PICKER")
    }
  }

  /** @author Julio Chort */
  override fun onResume() {
    super.onResume()

    // Si viene de la pantalla siguiente, hay que cancelar que se vuelva a abrir el calendario
    // y se deben restablecer los valores ingresados por el usuario
    if (sharedViewModelProximaPantalla.vieneDePantallaConfirmacionReserva) {
      volverTodoAComoEstaba()
    }
  }

  /** @author Julio Chort */
  private fun volverTodoAComoEstaba() {
    // Cantidad de personas
    binding.textInputTamanioGrupo.text?.clear()
    binding.textInputTamanioGrupo.text?.insert(
      0,
      sharedViewModelProximaPantalla.cantidadPersonas.toString()
    )

    // Horarios
    limpiarDatosViejosHorarios()
    viewModel.restablecerHorariosComoEstaban()
    chequearChipAntesSeleccionado(sharedViewModelProximaPantalla.indexChipSeleccionado)

    // Se realizó toda la lógica, ahora ya no viene de pantalla confirmacion reserva
    sharedViewModelProximaPantalla.vieneDePantallaConfirmacionReserva = false
  }

  /**
   * Vuelve a la [PantallaBar] anterior, limpiando los datos de los horarios creados por si quiere
   * volver a reservar.
   *
   * @author Julio Chort
   */
  private fun volverAtras() {
    limpiarDatosViejosHorarios()
    NavHostFragment.findNavController(this).popBackStack()
  }

  /**
   * Se navega al siguiente fragmento [PantallaConfirmacionReserva].
   *
   * @author Julio Chort
   */
  private fun navegarAConfirmarReserva() {
    copiarDatosAlViewModelCompartido()

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaCrearReserva_to_pantallaConfirmacionReserva)
  }

  /**
   * Se sacan todos los [Chip] y [TextView] agregados al [ChipGroup] y a la vez se desactiva el
   * botón de reservar para no permitir que quede seleccionado un [Horario] antiguo.
   *
   * @author Julio Chort
   */
  private fun limpiarDatosViejosHorarios() {
    binding.chipGroup.removeAllViews()
    binding.botonCrearReserva.isEnabled = false
  }

  /**
   * Se relaciona [PantallaCrearReserva] con su [PantallaCrearReservaViewModel]
   *
   * @author Julio Chort
   */
  private fun relacionarFragmentoConViewModel() {

    var indexChipGroupDesayuno = 0
    var indexChipGroupAlmuerzo = 0
    var indexChipGroupMerienda = 0
    var indexChipGroupCena = 0
    viewModel.todosChipsInhabilitadosDesayuno = true
    viewModel.todosChipsInhabilitadosAlmuerzo = true
    viewModel.todosChipsInhabilitadosMerienda = true
    viewModel.todosChipsInhabilitadosCena = true

    viewModel.textoCantidadPersonas.observe(viewLifecycleOwner) { _ ->
      binding.textInputTamanioGrupo.text?.let {
        it.replace(0, it.length, viewModel.cantidadPersonas.toString())
      }
    }

    viewModel.fechaReserva.observe(viewLifecycleOwner) { fecha ->
      binding.textInputFechaReserva.text?.let { editable ->
        editable.replace(
          0,
          editable.length,
          fecha
            ?.let { viewModel.fechaAFormatoTexto(fecha) }
            ?: requireContext().resources.getString(R.string.pantalla_reservar_texto_fecha_reserva))
      }
    }

    binding.textInputTamanioGrupo.doAfterTextChanged { text ->
      if (text.toString().isEmpty()) {
        binding.textFieldTamanioGrupo.error = "Se debe ingresar una cantidad de personas"
      } else {
        binding.textFieldTamanioGrupo.error = null
      }
      viewModel.handleInputCantidadPersonas(text.toString())
    }

    viewModel.habilitarBotonAgregarPersona.observe(viewLifecycleOwner) { banderaHabilitar ->
      binding.botonAgregarPersona.isEnabled = banderaHabilitar
    }

    viewModel.habilitarBotonQuitarPersona.observe(viewLifecycleOwner) { banderaHabilitar ->
      binding.botonQuitarPersona.isEnabled = banderaHabilitar
    }

    viewModel.habilitarTextFieldFechaReserva.observe(viewLifecycleOwner) { banderaHabilitar ->
      binding.textInputFechaReserva.isEnabled = banderaHabilitar
      binding.textFieldFechaReserva.isEnabled = banderaHabilitar
    }

    viewModel.horariosDesayuno.observe(viewLifecycleOwner) { listaHorarios ->
      // Si el restaurante tiene al menos un horario para ese tipo de comida
      if (listaHorarios.isNotEmpty()) {
        // En cada tipo de comida se coloca un indexChipGroup para identificarlo entre
        // todos los elementos del mismo
        indexChipGroupDesayuno = 0
        // Se valida si todos los chips del ChipGroup llegan a estar deshabilitados, para
        // mostrar un dialog si eso pasara
        viewModel.todosChipsInhabilitadosDesayuno = true

        // Si se limpió el ChipGroup, se eliminan todos los elementos anteriores y se resetea la bandera
        if (viewModel.clearedChipGroup.value!!) {
          binding.chipGroup.removeAllViews()
          viewModel.chipGroupCleared()
        }

        // Se crea un título para el tipo comida y se asigna el ancho como "MATCH_PARENT"
        // para que ocupe la totalidad  del renglón
        agregarTituloAlChipGroup(getString(R.string.pantalla_reservar_titulo_desayuno))
        // Aumenta el index en 1 porque ahora existe el TextView dentro del ChipGroup
        indexChipGroupDesayuno++

        // Se añaden los horarios como Chips seleccionables y se reasigna el valor del index
        // para que los elementos siguientes tengan la referencia de los anteriores
        indexChipGroupDesayuno =
          agregarChipsAlChipGroup(
            listaHorarios,
            indexChipGroupDesayuno,
            getString(R.string.pantalla_reservar_titulo_desayuno),
          )
      }
    }

    viewModel.horariosAlmuerzo.observe(viewLifecycleOwner) { listaHorarios ->
      if (listaHorarios.isNotEmpty()) {
        indexChipGroupAlmuerzo = 0
        viewModel.todosChipsInhabilitadosAlmuerzo = true

        if (viewModel.clearedChipGroup.value!!) {
          binding.chipGroup.removeAllViews()
          viewModel.chipGroupCleared()
        }

        agregarTituloAlChipGroup(getString(R.string.pantalla_reservar_titulo_almuerzo))
        indexChipGroupAlmuerzo++

        val indexEnChipGroupTotal = (indexChipGroupDesayuno + indexChipGroupAlmuerzo)

        indexChipGroupAlmuerzo =
          agregarChipsAlChipGroup(
            listaHorarios,
            indexEnChipGroupTotal,
            getString(R.string.pantalla_reservar_titulo_almuerzo),
          )
      }
    }

    viewModel.horariosMerienda.observe(viewLifecycleOwner) { listaHorarios ->
      if (listaHorarios.isNotEmpty()) {
        indexChipGroupMerienda = 0
        viewModel.todosChipsInhabilitadosMerienda = true

        if (viewModel.clearedChipGroup.value!!) {
          binding.chipGroup.removeAllViews()
          viewModel.chipGroupCleared()
        }

        agregarTituloAlChipGroup(getString(R.string.pantalla_reservar_titulo_merienda))
        indexChipGroupMerienda++

        val indexEnChipGroupTotal =
          (indexChipGroupDesayuno + indexChipGroupAlmuerzo + indexChipGroupMerienda)

        indexChipGroupMerienda =
          agregarChipsAlChipGroup(
            listaHorarios,
            indexEnChipGroupTotal,
            getString(R.string.pantalla_reservar_titulo_merienda),
          )
      }
    }

    viewModel.horariosCena.observe(viewLifecycleOwner) { listaHorarios ->
      if (listaHorarios.isNotEmpty()) {
        indexChipGroupCena = 0
        viewModel.todosChipsInhabilitadosCena = true

        if (viewModel.clearedChipGroup.value!!) {
          binding.chipGroup.removeAllViews()
          viewModel.chipGroupCleared()
        }

        agregarTituloAlChipGroup(getString(R.string.pantalla_reservar_titulo_cena))
        indexChipGroupCena++

        val indexEnChipGroupTotal =
          (indexChipGroupDesayuno +
              indexChipGroupAlmuerzo +
              indexChipGroupMerienda +
              indexChipGroupCena)

        indexChipGroupCena =
          agregarChipsAlChipGroup(
            listaHorarios,
            indexEnChipGroupTotal,
            getString(R.string.pantalla_reservar_titulo_cena),
          )
      }
    }

    viewModel.habilitarDialogTodosDeshabilitados.observe(viewLifecycleOwner) { banderaHabilitar ->
      if (banderaHabilitar) {
        MaterialAlertDialogBuilder(this.requireContext())
          .setTitle(R.string.pantalla_reservar_titulo_dialog)
          .setMessage(R.string.pantalla_reservar_texto_dialog)
          .setPositiveButton(R.string.boton_aceptar) { _, _ -> limpiarDatosViejosHorarios() }
          .setOnDismissListener { limpiarDatosViejosHorarios() }
          .show()
      }
    }
  }

  /** @author Julio Chort */
  private fun agregarTituloAlChipGroup(texto: String) {
    val titulo = crearTitulo(texto)
    titulo.layoutParams =
      ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
      )
    binding.chipGroup.addView(titulo)
  }

  /** @author Julio Chort */
  private fun agregarChipsAlChipGroup(
    mapHorariosMaxPersonas: Map<List<String>, Int>,
    indexHastaElMomento: Int,
    tipoComida: String,
  ): Int {
    var index = indexHastaElMomento

    for ((listaHorarios, maxPersonas) in mapHorariosMaxPersonas) {
      for (horario in listaHorarios) {
        if (noEsDeMadrugada(horario)) {
          val chip = crearChip(horario.substring(0, 5), index)
          if (!viewModel.esHorarioHabilitado(maxPersonas)) {
            chip.isEnabled = false
          } else {
            deshabilitarBanderaChipsInhabilitados(tipoComida)
          }
          binding.chipGroup.addView(chip)
          index++
        }
      }
    }

    return index
  }

  /** @author Julio Chort */
  private fun deshabilitarBanderaChipsInhabilitados(tipoComida: String) {
    when (tipoComida) {
      "Desayuno" -> viewModel.todosChipsInhabilitadosDesayuno = false
      "Almuerzo" -> viewModel.todosChipsInhabilitadosAlmuerzo = false
      "Merienda" -> viewModel.todosChipsInhabilitadosMerienda = false
      "Cena" -> viewModel.todosChipsInhabilitadosCena = false
    }
  }

  /** @author Julio Chort */
  private fun noEsDeMadrugada(hora: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val localTime = LocalTime.parse(hora, formatter)
    return !localTime.isBefore(LocalTime.of(5, 0))
  }

  /** @author Julio Chort */
  private fun chequearChipAntesSeleccionado(indexChipSeleccionado: Int) {

    (binding.chipGroup[indexChipSeleccionado] as Chip).isChecked = true
    binding.botonCrearReserva.isEnabled = true
  }

  private fun checkearHabilitarReserva() {
    if (viewModel.fechaReserva.value != null)
      binding.botonCrearReserva.isEnabled = true
  }

  /**
   * Se infla un [Chip] a partir del binding [ItemChipHorarioBinding] con el texto pasado por
   * parámetro
   *
   * @author Julio Chort
   */
  private fun crearChip(texto: String, index: Int): Chip {
    val chip = ItemChipHorarioBinding.inflate(layoutInflater).root
    chip.text = texto
    chip.setOnClickListener {
      if (chip.isChecked) {
        checkearHabilitarReserva()
        viewModel.datosDelChipSeleccionado(chip.text.toString(), index)
      } else {
        binding.botonCrearReserva.isEnabled = false
      }
    }
    return chip
  }

  /**
   * Se infla un [TextView] a partir del binding [ItemTituloHorarioBinding] con el texto pasado por
   * parámetro
   *
   * @author Julio Chort
   */
  private fun crearTitulo(texto: String): TextView {
    val titulo = ItemTituloHorarioBinding.inflate(layoutInflater).root
    titulo.text = texto
    return titulo
  }

  /**
   * Se copian los atributos de [PantallaCrearReservaViewModel] dentro de
   * [CrearReservaSharedViewModel].
   *
   * @author Julio Chort
   */
  private fun copiarDatosAlViewModelCompartido() {
    sharedViewModelProximaPantalla.cantidadPersonas = viewModel.cantidadPersonas
    sharedViewModelProximaPantalla.textoFechaReserva =
      viewModel.fechaReserva.value!!.let { fecha -> viewModel.fechaAFormatoTexto(fecha) }

    sharedViewModelProximaPantalla.fechaReserva = viewModel.fechaReserva.value!!
    sharedViewModelProximaPantalla.horaReserva = viewModel.horaReserva

    sharedViewModelProximaPantalla.barSeleccionado = sharedViewModelBarAReservar.barSeleccionado

    sharedViewModelProximaPantalla.indexChipSeleccionado = viewModel.indexChipSeleccionado
  }
}
