package com.barapp.ui.pantallas

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.databinding.FragmentPantallaResumenReservaBinding
import com.barapp.viewModels.MainActivityViewModel
import java.time.LocalDate

class PantallaResumenReserva : Fragment() {

  private lateinit var binding: FragmentPantallaResumenReservaBinding

  private val activitySharedViewModel: MainActivityViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {

    binding = FragmentPantallaResumenReservaBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.toolbar.setNavigationOnClickListener { volverAtras() }

    binding.botonCancelarReserva.isEnabled = false

    activitySharedViewModel.reservaLD.observe(viewLifecycleOwner) { r ->
      binding.toolbar.subtitle = r.restaurante.nombre

      val cantPersonas = r.cantidadPersonas
      binding.textViewCantidadPersonas.text =
        if (cantPersonas == 1)
          getString(R.string.placeholder_persona, cantPersonas)
        else getString(R.string.placeholder_personas, cantPersonas)

      binding.textViewFechaReserva.text = fechaAFormatoTexto(r.getFechaAsLocalDate())
      binding.textViewHoraReserva.text = r.horario.horario.substring(0, 5)
      binding.botonCancelarReserva.isEnabled = true
    }

    activitySharedViewModel.usuario.observe(viewLifecycleOwner) {usuario ->
      binding.textViewNombre.text =
        getString(
          R.string.placeholder_nombre_apellido,
          usuario.nombre,
          usuario.apellido,
        )
      binding.textViewMail.text = usuario.detalleUsuario?.mail
      binding.textViewTelefono.text =
        getString(
          R.string.placeholder_telefono_argentina,
          usuario.detalleUsuario?.telefono,
        )
    }

    binding.botonCancelarReserva.setOnClickListener { cancelarReserva() }
  }

  private fun volverAtras() {
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun cancelarReserva() {
    AlertDialog.Builder(requireContext())
      .setTitle(getString(R.string.titulo_dialogo_cancelar_reserva))
      .setMessage(getString(R.string.mensaje_dialogo_cancelar_reserva))
      .setPositiveButton(getString(R.string.boton_si)) { dialog, _ ->
        activitySharedViewModel.cancelarReserva {
          dialog.dismiss()
          NavHostFragment.findNavController(this).popBackStack()
        }
      }
      .setNegativeButton(getString(R.string.boton_no)) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
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
