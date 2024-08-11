package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
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
import com.barapp.util.Interpolator
import com.barapp.viewModels.sharedViewModels.BarAReservarSharedViewModel
import com.barapp.viewModels.sharedViewModels.CrearReservaSharedViewModel
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaCrearReservaViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import java.time.LocalTime

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

  // private var indexChipGroup : Int = 0

  private lateinit var binding: FragmentPantallaCrearReservaBinding

  private val viewModel: PantallaCrearReservaViewModel by viewModels()

  private val sharedViewModelProximaPantalla: CrearReservaSharedViewModel by
    navGraphViewModels(R.id.pantallaCrearReserva)

  private val sharedViewModelBarAReservar: BarAReservarSharedViewModel by
    navGraphViewModels(R.id.pantallaNavegacionPrincipal)

  private val activitySharedViewModel: MainActivityViewModel by activityViewModels()

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

    binding.botonAgregarPersona.setOnClickListener { viewModel.agregarPersona() }

    binding.botonQuitarPersona.setOnClickListener { viewModel.quitarPersona() }

    binding.cardViewFechaReserva.setOnClickListener {
      limpiarDatosViejosHorarios()
      viewModel.crearDatePicker()
    }

    binding.botonCrearReserva.isEnabled = false

    binding.botonCrearReserva.setOnClickListener {
      viewModel.pasarChipSeleccionadoAHorario()
      navegarAConfirmarReserva()
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
    binding.txtViewCantidadPersonas.text = sharedViewModelProximaPantalla.textoCantidadPersonas
    viewModel.cantidadDePersonasDeTextoANumero(sharedViewModelProximaPantalla.textoCantidadPersonas)

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

    viewModel.cantidadDePersonasDeTextoANumero(binding.txtViewCantidadPersonas.text.toString())

    viewModel.textoCantidadPersonas.observe(viewLifecycleOwner) { textoCantPersonas ->
      binding.txtViewCantidadPersonas.text = textoCantPersonas
    }

    viewModel.textoFechaReserva.observe(viewLifecycleOwner) { textoFecha ->
      binding.textViewFechaReserva.text = textoFecha
    }

    viewModel.habilitarBotonAgregarPersona.observe(viewLifecycleOwner) { banderaHabilitar ->
      binding.botonAgregarPersona.isEnabled = banderaHabilitar
    }

    viewModel.habilitarBotonQuitarPersona.observe(viewLifecycleOwner) { banderaHabilitar ->
      binding.botonQuitarPersona.isEnabled = banderaHabilitar
    }

    viewModel.datePicker.observe(viewLifecycleOwner) { datePicker ->
      if (!sharedViewModelProximaPantalla.vieneDePantallaConfirmacionReserva) {
        datePicker.show(parentFragmentManager, "DATE_PICKER")
      }
    }

    viewModel.habilitarCardDatePicker.observe(viewLifecycleOwner) { banderaHabilitar ->
      binding.cardViewFechaReserva.isEnabled = banderaHabilitar
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
          .setOnDismissListener() { limpiarDatosViejosHorarios() }
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
    listaHorarios: List<Horario>,
    indexHastaElMomento: Int,
    tipoComida: String,
  ): Int {
    var index = indexHastaElMomento

    for (horario in listaHorarios) {
      if (noEsDeMadrugada(horario.getHorarioAsLocalTime())) {
        val chip = crearChip(horario.horario.substring(0,5), index)
        if (viewModel.comprobarSiEsHorarioHabilitado(horario)) {
          chip.isEnabled = false
        } else {
          deshabilitarBanderaChipsInhabilitados(tipoComida)
        }
        binding.chipGroup.addView(chip)
        index++
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
  private fun noEsDeMadrugada(hora: LocalTime): Boolean {
    return !hora.isBefore(LocalTime.of(5, 0))
  }

  /** @author Julio Chort */
  private fun chequearChipAntesSeleccionado(indexChipSeleccionado: Int) {

    (binding.chipGroup[indexChipSeleccionado] as Chip).isChecked = true
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
        binding.botonCrearReserva.isEnabled = true
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
    sharedViewModelProximaPantalla.textoCantidadPersonas =
      viewModel.textoCantidadPersonas.value.toString()
    sharedViewModelProximaPantalla.textoFechaReserva = viewModel.textoFechaReserva.value.toString()

    sharedViewModelProximaPantalla.cantidadPersonas = viewModel.cantidadPersonas
    sharedViewModelProximaPantalla.fechaReserva = viewModel.fechaReserva
    sharedViewModelProximaPantalla.horaReserva = viewModel.horaReserva

    sharedViewModelProximaPantalla.barSeleccionado = sharedViewModelBarAReservar.barSeleccionado

    sharedViewModelProximaPantalla.indexChipSeleccionado = viewModel.indexChipSeleccionado
  }
}
