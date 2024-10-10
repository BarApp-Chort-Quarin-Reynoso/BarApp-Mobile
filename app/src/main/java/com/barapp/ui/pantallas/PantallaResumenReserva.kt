package com.barapp.ui.pantallas

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import com.barapp.R
import com.barapp.barapp.model.Reserva
import com.barapp.data.entities.RestauranteInfoQR
import com.barapp.data.utils.FirestoreCallback
import com.barapp.databinding.FragmentPantallaResumenReservaBinding
import com.barapp.model.EstadoReserva
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.sharedViewModels.RestauranteSeleccionadoSharedViewModel
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber
import java.time.LocalDate

class PantallaResumenReserva : Fragment() {

  private lateinit var binding: FragmentPantallaResumenReservaBinding

  private val activitySharedViewModel: MainActivityViewModel by activityViewModels()

  private val restauranteSeleccionadoViewModel: RestauranteSeleccionadoSharedViewModel by navGraphViewModels(R.id.pantallaResumenReserva)

  private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>

  private val gson = Gson()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {

    binding = FragmentPantallaResumenReservaBinding.inflate(inflater, container, false)

    binding.root.transitionName = arguments?.getString("transition_name")

    sharedElementEnterTransition = MaterialContainerTransform()

    barcodeLauncher = registerForActivityResult(
      ScanContract()
    ) { result: ScanIntentResult ->
      if (result.contents == null) {
        Toast.makeText(this.context, "Cancelado", Toast.LENGTH_LONG).show()
      } else {
        val restauranteInfoQR = gson.fromJson(result.contents, RestauranteInfoQR::class.java)

        // Guardado de datos de sesion
        val prefs = this.requireContext().getSharedPreferences(getString(R.string.shared_pref_file), Context.MODE_PRIVATE)
        val idUsuario = prefs.getString("idUsuario", "")

        if (idUsuario !== "") {
          activitySharedViewModel.concretarReserva(idUsuario!!, restauranteInfoQR, object : FirestoreCallback<Reserva> {
            override fun onSuccess(result: Reserva) {
              if (result.estado == EstadoReserva.PENDIENTE)  {
                Toast.makeText(context, "No se pudo concretar la reserva.", Toast.LENGTH_LONG).show()
              } else {
                activitySharedViewModel.setReservaSync(result)

                binding.botonConcretarReserva.visibility = View.GONE
                binding.botonCancelarReserva.visibility = View.GONE
                binding.botonIrAlDetalle.visibility = View.VISIBLE

                Toast.makeText(context, "La reserva se ha concretado con éxito.", Toast.LENGTH_LONG).show()
              }
            }
            override fun onError(exception: Throwable) {
              Timber.d("Error al concretar una reserva")
            }
          })
        }
      }
    }

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.postponeEnterTransition()
    view.doOnPreDraw { startPostponedEnterTransition() }

    binding.toolbar.setNavigationOnClickListener { volverAtras() }

    binding.botonCancelarReserva.isEnabled = false

    activitySharedViewModel.reservaLD.observe(viewLifecycleOwner) { r ->
      binding.toolbar.subtitle = r.restaurante.nombre
      if (r.estado == EstadoReserva.CONCRETADA) {
        binding.botonConcretarReserva.visibility = View.GONE
        binding.botonCancelarReserva.visibility = View.GONE
        binding.botonIrAlDetalle.visibility = View.VISIBLE
      }

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

    binding.botonConcretarReserva.setOnClickListener {
      val scanOptions = ScanOptions()
      scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
      scanOptions.setBeepEnabled(false)
      scanOptions.setOrientationLocked(false)
      scanOptions.setCameraId(0)
      scanOptions.setBarcodeImageEnabled(true)
      scanOptions.setPrompt("Escanea el código QR")
      scanOptions.setBarcodeImageEnabled(true)
      barcodeLauncher.launch(scanOptions)
    }
    binding.botonCancelarReserva.setOnClickListener { cancelarReserva() }

    binding.botonIrAlDetalle.setOnClickListener {
      val bundle = Bundle()
      bundle.putInt("origen", PantallaBar.ORIGEN_RESUMEN_RESERVA)

      restauranteSeleccionadoViewModel.buscarRestaurante(activitySharedViewModel.reservaLD.value!!.restaurante.id)
      restauranteSeleccionadoViewModel.distancia = 0

      exitTransition = Hold()

      NavHostFragment.findNavController(this)
        .navigate(R.id.action_pantallaResumenReserva_to_pantallaBar, bundle)
    }
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
