package com.barapp.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import com.barapp.R
import com.barapp.databinding.FragmentPantallaUbicacionBarBinding
import com.barapp.util.Dialogs
import com.barapp.util.Interpolator
import com.barapp.util.Maps
import com.barapp.util.MarkerUtils
import com.barapp.viewModels.sharedViewModels.UbicacionBarSharedViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Esta clase es un [Fragment] que se utiliza para mostrar la ubicación de un bar especifico y la
 * ubicación actual donde se encuentra el dispositivo.
 *
 * Esta pantalla cuenta con un botón que lleva al usuario a Google Maps y se le muestran las
 * indicaciones para llegar al bar.
 *
 * @author Federico Quarin
 */
class PantallaUbicacionBar : Fragment() {
  // TODO Corregir manejo de permisos
  private lateinit var binding: FragmentPantallaUbicacionBarBinding
  private var map: GoogleMap? = null
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  private val ubicacionViewModel: UbicacionBarSharedViewModel by
    navGraphViewModels(R.id.pantallaBar)

  // ActivityRequestLauncher que se usa para pedir los permisos
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        initMapa()
      } else {
        volverAtras()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition =
      MaterialSharedAxis(MaterialSharedAxis.X, true)
        .setInterpolator(Interpolator.emphasizedInterpolator())
    returnTransition =
      MaterialSharedAxis(MaterialSharedAxis.X, false)
        .setInterpolator(Interpolator.emphasizedInterpolator())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaUbicacionBarBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    Timber.e("Ejecutandose onViewCreated")

    lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        map = mapFragment.awaitMap()
        onMapReady()
      }
    }

    binding.toolbar.setNavigationOnClickListener { volverAtras() }

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    binding.fabIndicaciones.setOnClickListener { abrirGoogleMaps() }
  }

  override fun onDestroyView() {
    super.onDestroyView()

    map = null
    Timber.e("Mapa destruido")
  }

  private fun volverAtras() {
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun abrirGoogleMaps() {
    val navigationIntentUri =
      Uri.parse(
        "geo:0,0?q=" +
          ubicacionViewModel.ubicacionRestaurante.latitud +
          "," +
          ubicacionViewModel.ubicacionRestaurante.longitud +
          "(" +
          ubicacionViewModel.nombreRestaurante +
          ")"
      )
    val mapIntent = Intent(Intent.ACTION_VIEW, navigationIntentUri)
    startActivity(mapIntent)
  }

  fun onMapReady() {
    if (!Maps.tienePermisosLocalizacion(requireContext())) {
      if (
        ActivityCompat.shouldShowRequestPermissionRationale(
          requireActivity(),
          Manifest.permission.ACCESS_FINE_LOCATION,
        )
      ) {
        Dialogs.rationalPermissionRequestDialog(
          requireContext(),
          { _, _ -> pedirPermisosUbicacion() },
          { _, _ -> volverAtras() },
        )
      } else {
        pedirPermisosUbicacion()
      }

      return
    }

    initMapa()
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
   * Actualiza el mapa para mostrar la ubicación actual del usuario y la del bar a mostrar. La
   * porción de mapa mostrada se adapta a las ubicaciones que se muestran.
   *
   * Solo se debe ejecutar esta función cuando el mapa se encuentre cargado correctamente y se debe
   * contar con los permisos [Manifest.permission.ACCESS_FINE_LOCATION] y
   * [Manifest.permission.ACCESS_COARSE_LOCATION]
   *
   * @author Federico Quarin
   */
  @SuppressLint("MissingPermission")
  private fun initMapa() {
    map!!.run {
      if (Maps.tienePermisosLocalizacion(requireContext())) {
        Timber.e("Se esta actualizando mapa")
        this.uiSettings.isMyLocationButtonEnabled = false
        this.uiSettings.isMapToolbarEnabled = false

        val markerSize = resources.getInteger(R.integer.marker_size)
        val logoSize = 78 * markerSize / 128

        Glide.with(requireContext())
          .asBitmap()
          .load(ubicacionViewModel.logo)
          .apply(RequestOptions.circleCropTransform())
          .into(
            object : CustomTarget<Bitmap>(logoSize, logoSize) {
              override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val marker = MarkerUtils.construirMarcador(resource, requireContext())

                this@run.addMarker(
                  MarkerOptions()
                    .position(
                      LatLng(
                        ubicacionViewModel.ubicacionRestaurante.latitud,
                        ubicacionViewModel.ubicacionRestaurante.longitud,
                      )
                    )
                    .icon(BitmapDescriptorFactory.fromBitmap(marker))
                )
              }

              override fun onLoadCleared(placeholder: Drawable?) {
                // this is called when imageView is cleared on lifecycle call or for
                // some other reason.
                // if you are referencing the bitmap somewhere else too other than this imageView
                // clear it here as you can no longer have the bitmap
              }
            }
          )

        fusedLocationClient
          .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
          .addOnSuccessListener { location ->
            location?.let {
              Timber.e("%s, %s", it.latitude, it.longitude)

              this.addMarker(
                MarkerOptions()
                  .position(LatLng(it.latitude, it.longitude))
                  .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
              )

              val limite =
                crearAreaAMostrar(
                  LatLng(it.latitude, it.longitude),
                  LatLng(
                    ubicacionViewModel.ubicacionRestaurante.latitud,
                    ubicacionViewModel.ubicacionRestaurante.longitud,
                  ),
                )

              this.animateCamera(CameraUpdateFactory.newLatLngBounds(limite, 200))
            }
              ?: let {
                Snackbar.make(
                    requireView(),
                    R.string.error_ubicacion_no_disponible,
                    Snackbar.LENGTH_SHORT,
                  )
                  .show()
                it.animateCamera(
                  CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                      ubicacionViewModel.ubicacionRestaurante.latitud,
                      ubicacionViewModel.ubicacionRestaurante.longitud,
                    ),
                    15F,
                  )
                )
              }
          }
      }
    }
  }

  /**
   * Toma dos puntos de tipo [LatLng] y retorna el area minima necesaria para mostrarlos, en formato
   * [LatLngBounds]
   *
   * @param ubicacion1 Primera ubicacion a mostrar
   * @param ubicacion2 Segunda ubicacion a mostrar
   * @return [LatLngBounds] que abarca ambas ubicaciones
   * @author Federico Quarin
   */
  private fun crearAreaAMostrar(ubicacion1: LatLng, ubicacion2: LatLng): LatLngBounds {
    val south: Double
    val north: Double
    val east: Double
    val west: Double

    if (ubicacion1.latitude > ubicacion2.latitude) {
      north = ubicacion1.latitude
      south = ubicacion2.latitude
    } else {
      north = ubicacion2.latitude
      south = ubicacion1.latitude
    }

    if (ubicacion1.longitude > ubicacion2.longitude) {
      east = ubicacion1.longitude
      west = ubicacion2.longitude
    } else {
      east = ubicacion2.longitude
      west = ubicacion1.longitude
    }

    return LatLngBounds(LatLng(south, west), LatLng(north, east))
  }
}
