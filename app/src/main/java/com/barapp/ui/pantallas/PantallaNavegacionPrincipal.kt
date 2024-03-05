package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.NavigationUI
import com.barapp.ui.MainActivity
import com.barapp.R
import com.barapp.databinding.FragmentPantallaNavegacionPrincipalBinding
import com.barapp.model.Restaurante
import com.barapp.util.interfaces.OnRestauranteClicked
import com.barapp.util.interfaces.OnSnackbarShowed
import com.barapp.viewModels.sharedViewModels.RestauranteSeleccionadoSharedViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.Hold
import timber.log.Timber

class PantallaNavegacionPrincipal : Fragment(), OnRestauranteClicked {
  private lateinit var binding: FragmentPantallaNavegacionPrincipalBinding

  private val restauranteSeleccionadoViewModel: RestauranteSeleccionadoSharedViewModel by
    navGraphViewModels(R.id.pantallaNavegacionPrincipal)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    exitTransition = Hold()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaNavegacionPrincipalBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Se ejecuta este codigo para que funcione la transicion
    postponeEnterTransition()
    (requireView().parent as ViewGroup).viewTreeObserver.addOnPreDrawListener {
      startPostponedEnterTransition()
      true
    }

    val navController =
      NavHostFragment.findNavController(binding.fragmentContainerView.getFragment())

    NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

    arguments?.getInt("origen")?.let { origenActividad ->
      if (origenActividad == MainActivity.DESDE_NOTIFICACION) {
        Timber.i("Se navega a pantalla reservas")
        navController.navigate(R.id.action_global_pantallaMisReservas)
      }
      if (origenActividad == MainActivity.DESDE_CONFIRMACION_RESERVA) {
        val fragment =
          binding.fragmentContainerView
            .getFragment<NavHostFragment>()
            .childFragmentManager
            .fragments
            .last() as OnSnackbarShowed

        // Muestra un snackbar informando al usuario de que su reserva fue realizada exitosamente
        val snackbar =
          Snackbar.make(
              requireView(),
              getString(R.string.pantalla_confirmacion_reservas_snackbar_texto),
              Snackbar.LENGTH_LONG,
            )
            .setDuration(Snackbar.LENGTH_LONG)

        fragment.showSnackbar(snackbar)
      }
    }

    arguments = null
  }

  override fun onRestauranteClicked(
    restaurante: Restaurante,
    transitionView: View,
    distancia: Int?,
  ) {
    val bundle = Bundle()
    bundle.putInt("origen", PantallaBar.ORIGEN_PANTALLA_PRINCIPAL)
    bundle.putString("transition_name", transitionView.transitionName)

    val extras = FragmentNavigatorExtras(transitionView to (transitionView.transitionName ?: ""))

    restauranteSeleccionadoViewModel.restaurante = restaurante
    restauranteSeleccionadoViewModel.distancia = distancia

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaBar, bundle, null, extras)
  }
}
