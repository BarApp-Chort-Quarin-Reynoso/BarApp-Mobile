package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.sharedViewModels.RestauranteSeleccionadoSharedViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import timber.log.Timber

class PantallaNavegacionPrincipal :
  Fragment(),
  OnRestauranteClicked,
  PantallaPrincipal.OnFabBuscarClicked,
  OnReservaClicked {
  private lateinit var binding: FragmentPantallaNavegacionPrincipalBinding

  private val restauranteSeleccionadoViewModel: RestauranteSeleccionadoSharedViewModel by
  navGraphViewModels(R.id.pantallaNavegacionPrincipal)
  private val activitySharedViewModel: MainActivityViewModel by activityViewModels {
    MainActivityViewModel.Factory(null)
  }

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

    activitySharedViewModel.origen?.let {
      val intOrigen = it.toInt()

      if (intOrigen == MainActivity.NAVEGACION_DESDE_NOTIFICACION_RESERVA) {
        val idReserva = requireActivity().intent.extras!!.getString("idReserva")!!
        activitySharedViewModel.searchReserva(idReserva)
        Timber.i("Se navega a reserva de bar {}", idReserva)
        navController.navigate(R.id.action_global_pantallaMisReservas)
        NavHostFragment.findNavController(this)
          .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaResumenReserva)
      } else if (intOrigen == MainActivity.NAVEGACION_DESDE_NOTIFICACION_OPINAR) {
        val idReserva = requireActivity().intent.extras!!.getString("idReserva")!!
        activitySharedViewModel.searchReserva(idReserva)
        Timber.i("Se navega a opinar de bar {}", idReserva)
        navController.navigate(R.id.action_global_pantallaMisReservas)
        NavHostFragment.findNavController(this)
          .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaCrearOpinion)
      }

      activitySharedViewModel.origen = null
    }

    arguments?.getInt("origen")?.let { origenActividad ->
      if (origenActividad == MainActivity.NAVEGACION_DESDE_CONFIRMACION_RESERVA) {
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

    exitTransition = Hold().apply {
      excludeTarget(transitionView, true)
    }
    reenterTransition = Hold().apply {
      excludeTarget(transitionView, true)
    }

    restauranteSeleccionadoViewModel.setSyncRestaurante(restaurante)
    restauranteSeleccionadoViewModel.distancia = distancia

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaBar, bundle, null, extras)
  }

  override fun onFabBuscarClicked(fabBuscar: View) {
    val extras = FragmentNavigatorExtras(fabBuscar to "transition_pantalla_buscar")

    exitTransition = Hold()
    reenterTransition = null

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaBusqueda, null, null, extras)
  }

  override fun onOpinarButtonClicked(transitionView: View) {
    exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaNavegacionPrincipal_to_pantallaCrearOpinion)
  }

  override fun onReservaClicked(transitionView: View) {
    val bundle = Bundle()
    bundle.putString("transition_name", transitionView.transitionName)

    exitTransition = Hold().apply {
      excludeTarget(transitionView, true)
    }
    reenterTransition = null

    val extras = FragmentNavigatorExtras(transitionView to (transitionView.transitionName ?: ""))

    NavHostFragment.findNavController(this)
      .navigate(
        R.id.action_pantallaNavegacionPrincipal_to_pantallaResumenReserva,
        bundle,
        null,
        extras
      )
  }
}
