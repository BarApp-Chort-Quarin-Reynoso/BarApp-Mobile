package com.barapp.ui.pantallas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import com.barapp.R
import com.barapp.databinding.FragmentPantallaConfirmacionReservaBinding
import com.barapp.ui.MainActivity
import com.barapp.util.notifications.NotificacionReservaManager
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaConfirmacionViewModel
import com.barapp.viewModels.sharedViewModels.CrearReservaSharedViewModel

class PantallaConfirmacionReserva : Fragment() {

  private lateinit var binding: FragmentPantallaConfirmacionReservaBinding

  private val viewModel: PantallaConfirmacionViewModel by viewModels()

  private val sharedViewModel: CrearReservaSharedViewModel by
    navGraphViewModels(R.id.pantallaCrearReserva)

  private val activitySharedViewModel: MainActivityViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {

    binding = FragmentPantallaConfirmacionReservaBinding.inflate(inflater, container, false)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    sharedViewModel.vieneDePantallaConfirmacionReserva = true

    binding.toolbar.setNavigationOnClickListener { volverAtras() }

    binding.toolbar.subtitle = sharedViewModel.barSeleccionado.nombre

    binding.textViewCantidadPersonas.text = sharedViewModel.textoCantidadPersonas
    binding.textViewFechaReserva.text = sharedViewModel.textoFechaReserva
    binding.textViewHoraReserva.text = sharedViewModel.horaReserva.horario

    // Voy a buscar en las shared preferences el id que tiene el usuario de la sesion actual
    val idUsuario =
      this.context
        ?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        ?.getString("idUsuario", null)

    binding.textViewNombre.text =
      getString(
        R.string.placeholder_nombre_apellido,
        activitySharedViewModel.usuario.value?.nombre,
        activitySharedViewModel.usuario.value?.apellido
      )
    binding.textViewMail.text = activitySharedViewModel.usuario.value?.detalleUsuario?.mail
    binding.textViewTelefono.text =
      getString(
        R.string.placeholder_telefono_argentina,
        activitySharedViewModel.usuario.value?.detalleUsuario?.telefono,
      )

    binding.botonConfirmarReserva.setOnClickListener {
      viewModel.usuario = activitySharedViewModel.usuario.value!!
      viewModel.barSeleccionado = sharedViewModel.barSeleccionado

      val reserva =
        viewModel.crearReserva(
          sharedViewModel.cantidadPersonas,
          sharedViewModel.fechaReserva,
          sharedViewModel.horaReserva,
        )

      NotificacionReservaManager.crearAlarma(requireContext(), reserva)

      volverAPantallaPrincipal()
    }
  }

  private fun volverAtras() {
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun volverAPantallaPrincipal() {
    val bundle = Bundle()
    bundle.putInt("origen", MainActivity.DESDE_CONFIRMACION_RESERVA)

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaConfirmacionReserva_to_pantallaNavegacionPrincipal, bundle)
  }
}
