package com.barapp.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.barapp.databinding.FragmentPantallaUbicacionBaresBinding
import com.barapp.util.CambiarPermisosEnConfiguracion
import com.barapp.util.Interpolator
import com.barapp.util.Maps
import com.barapp.viewModels.PantallaUbicacionBaresViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.transition.MaterialFadeThrough
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch

class PantallaUbicacionBares : Fragment() {
  private lateinit var binding: FragmentPantallaUbicacionBaresBinding
  private lateinit var map: GoogleMap

  /**
   * Esta variable se utiliza para chequear si ya se realizaron las dos solicitudes de permisos
   * permitidas.
   */
  private var sePidieronLosPermisosAmablemente: Boolean = false

  // ActivityRequestLauncher que se usa para pedir los permisos
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        mostrarMapa()
        actualizarMapa()
      } else {
        val seDebenPedirPermisosAmablemente =
          ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION,
          )
        /*
           Si ya se realizaron las dos peticiones permitidas, se le muestra al usuario
           un Dialog donde se le permite ir a los ajustes y cambiar los permisos
        */
        if (!sePidieronLosPermisosAmablemente && !seDebenPedirPermisosAmablemente)
          cambiarPermisosEnConfiguracion.showDialog()
      }
    }

  private lateinit var cambiarPermisosEnConfiguracion: CambiarPermisosEnConfiguracion

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition =
      MaterialFadeThrough().apply { interpolator = Interpolator.emphasizedInterpolator() }
    exitTransition =
      MaterialFadeThrough().apply { interpolator = Interpolator.emphasizedInterpolator() }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaUbicacionBaresBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    if (Maps.tienePermisosLocalizacion(requireContext())) mostrarMapa()

    lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        map = mapFragment.awaitMap()
        onMapReady()
      }
    }

    // Se crea el launcher para abrir la pestaña de configuraciones para modificar los permisos,
    // en caso de que se necesite utilizarlo
    cambiarPermisosEnConfiguracion =
      CambiarPermisosEnConfiguracion(requireActivity().activityResultRegistry, requireContext()) {
        if (Maps.tienePermisosLocalizacion(requireContext())) {
          mostrarMapa()
          actualizarMapa()
        }
      }

    /*
       Al presionar el boton se fija si se deben pedir los permisos racionalmente, en cuyo caso
       se mostrar directamente la ventana del sistema para pedir permisos
    */
    binding.botonPedirPermisos.setOnClickListener {
      sePidieronLosPermisosAmablemente =
        ActivityCompat.shouldShowRequestPermissionRationale(
          requireActivity(),
          Manifest.permission.ACCESS_FINE_LOCATION,
        )

      pedirPermisosUbicacion()
    }
  }

  /**
   * Este metodo comprueba si se cuenta con permisos de ubicacion. En caso de tenerlos se llama al
   * metodo [actualizarMapa]. En caso contrario se comprueba si se deben pedir amablemente los
   * permisos, en cuyo caso se deja al usuario que los pida al presionar el boton. De no tener que
   * hacerlo, se llama al metodo [pedirPermisosUbicacion], pero marcando la variable de
   * [sePidieronLosPermisosAmablemente]. Esto ultimo se hace para que, en caso de que el sistema
   * haya decidido que no se debe mostrar la ventana del sistema para pedir permisos, no se muestre
   * el dialog que lleva a ventana de configuracion para cambiar permisos manualmente.
   *
   * @author Federico Quarin
   */
  private fun onMapReady() {
    if (!Maps.tienePermisosLocalizacion(requireContext())) {
      if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) return
      sePidieronLosPermisosAmablemente = true
      pedirPermisosUbicacion()
      return
    }

    actualizarMapa()
  }

  @SuppressLint("MissingPermission")
  private fun actualizarMapa() {
    if (!Maps.tienePermisosLocalizacion(requireContext())) return

    map.isMyLocationEnabled = true
  }

  /**
   * Lanza una solicitud de permisos de ubicación
   *
   * @author Federico Quarin
   */
  private fun pedirPermisosUbicacion() {
    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
  }

  /**
   * Este metodo muestra el layout que contiene el mapa
   *
   * @author Federico Quarin
   */
  private fun mostrarMapa() {
    binding.pedirPermisosLayout.visibility = View.INVISIBLE
  }
}
