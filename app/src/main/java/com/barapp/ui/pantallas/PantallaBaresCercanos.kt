package com.barapp.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.barapp.R
import com.barapp.data.mappers.RestauranteMapper.toRestauranteUsuario
import com.barapp.data.repositories.DetalleUsuarioRepository
import com.barapp.data.repositories.RestauranteFavoritoRepository
import com.barapp.data.utils.FirestoreCallback
import com.barapp.databinding.FragmentPantallaUbicacionBaresBinding
import com.barapp.model.Restaurante
import com.barapp.util.CambiarPermisosEnConfiguracion
import com.barapp.util.Interpolator
import com.barapp.util.Maps
import com.barapp.util.MarkerUtils
import com.barapp.util.interfaces.OnRestauranteClicked
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaBaresCercanosViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.transition.MaterialFadeThrough
import com.google.maps.android.ktx.awaitMap
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.launch
import timber.log.Timber


class PantallaBaresCercanos : Fragment() {
  private lateinit var binding: FragmentPantallaUbicacionBaresBinding
  private lateinit var map: GoogleMap
  private lateinit var fusedLocationClient: FusedLocationProviderClient

  private val viewModel: PantallaBaresCercanosViewModel by viewModels()

  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
  private val detalleUsuarioRepository: DetalleUsuarioRepository = DetalleUsuarioRepository.instance
  private val restauranteFavoritoRepository: RestauranteFavoritoRepository =
    RestauranteFavoritoRepository.instance

  private lateinit var onRestauranteClicked: OnRestauranteClicked

  /**
   * Esta variable se utiliza para chequear si ya se realizaron las dos solicitudes de permisos
   * permitidas.
   */
  private var sePidieronLosPermisosAmablemente: Boolean = false

  private val restaurantesDibujados = HashSet<String>()

  // ActivityRequestLauncher que se usa para pedir los permisos
  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        ocultarLayoutPermisos()
        initMapa()
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
      MaterialFadeThrough().apply {
        interpolator = Interpolator.emphasizedInterpolator()
      }
    exitTransition =
      MaterialFadeThrough().apply {
        interpolator = Interpolator.emphasizedInterpolator()
      }
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

    if (Maps.tienePermisosLocalizacion(requireContext())) ocultarLayoutPermisos()

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
          ocultarLayoutPermisos()
          initMapa()
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

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

//    if (viewModel.restauranteSeleccionado != null) {
//      setBarInfoLayout(viewModel.restauranteSeleccionado!!)
//      binding.barCardView.visibility = View.VISIBLE
//    } else {
//      binding.barCardView.visibility = View.GONE
//    }
    onRestauranteClicked =
      (parentFragment as NavHostFragment).parentFragment as OnRestauranteClicked
  }

  /**
   * Comprueba si se cuenta con permisos de ubicacion. En caso de tenerlos se llama al
   * metodo [initMapa]. En caso contrario se comprueba si se deben pedir amablemente los
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

    initMapa()
  }

  @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
  /**
   * Permite inicializar el mapa luego de haber obtenido los permisos necesarios
   *
   * @author Federico Quarin
   */
  private fun initMapa() {
    if (!Maps.tienePermisosLocalizacion(requireContext())) return

    setLoading(true)
    map.isMyLocationEnabled = true
    map.uiSettings.isMapToolbarEnabled = false
    map.uiSettings.isTiltGesturesEnabled = false

    fusedLocationClient
      .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
      .addOnSuccessListener { location ->
        location?.let {
          setLoading(false)
          val userLocation = LatLng(location.latitude, location.longitude)
          map.animateCamera(
            viewModel.cameraPosition?.let { CameraUpdateFactory.newCameraPosition(it) }
              ?: CameraUpdateFactory.newLatLngZoom(
                userLocation,
                15.0f
              )
          )
        }
      }

    val markerSize = resources.getInteger(R.integer.marker_size)
    val logoSize = 78 * markerSize / 128
    viewModel.restaurantes.observe(viewLifecycleOwner) { restaurantes ->
      restaurantes.forEach { r ->
        if (restaurantesDibujados.contains(r.id))
          return@forEach

        Timber.i("Dibujando " + r.nombre)
        Glide.with(requireContext())
          .asBitmap()
          .load(r.logo)
          .apply(RequestOptions.circleCropTransform())
          .into(
            object : CustomTarget<Bitmap>(logoSize, logoSize) {
              override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val icon = MarkerUtils.construirMarcador(resource, requireContext())

                val marker = map.addMarker(
                  AdvancedMarkerOptions()
                    .position(LatLng(r.ubicacion.latitud, r.ubicacion.longitud))
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                )
                marker?.tag = r
                restaurantesDibujados.add(r.id)
              }

              override fun onLoadCleared(placeholder: Drawable?) {
              }
            }
          )
      }
    }
    map.setOnCameraIdleListener {
      viewModel.cameraPosition = map.cameraPosition
      val newBounds = map.projection.visibleRegion.latLngBounds
      viewModel.buscarBares(newBounds.northeast, newBounds.southwest)
    }
    map.setOnMapClickListener {
      binding.barCardView.visibility = View.GONE
    }
    map.setOnMarkerClickListener { ce ->
      setBarInfoLayout(ce.tag as Restaurante)
      binding.barCardView.visibility = View.VISIBLE
      Timber.e("Clicked: " + (ce.tag as Restaurante).nombre)
      return@setOnMarkerClickListener false
    }
    binding.barCardView.setOnClickListener { ce ->
      val restaurante = ce.tag as Restaurante
      ce.transitionName = restaurante.id
      onRestauranteClicked.onRestauranteClicked(restaurante, ce, null)
    }
  }

  private fun setBarInfoLayout(restaurante: Restaurante) {
    binding.barCardView.tag = restaurante
    binding.txtViewNombreRestaurante.text = restaurante.nombre
    binding.txtViewUbicacionRestaurante.text =
      String.format("%s %s", restaurante.ubicacion.calle, restaurante.ubicacion.numero)
    if (restaurante.cantidadOpiniones == 0) {
      binding.linearLayoutOpiniones.visibility = View.GONE
    } else {
      binding.txtViewPuntuacionRestaurante.text = restaurante.puntuacion.toString().substring(0, 3)
      binding.ratingBarPuntuacion.rating = restaurante.puntuacion.toFloat()
      "(${restaurante.cantidadOpiniones})".also { binding.textViewCantidadOpiniones.text = it }
    }
    Glide.with(requireContext())
      .load(restaurante.portada)
      .apply(
        RequestOptions.bitmapTransform(
          MultiTransformation(
            CenterCrop(),
            RoundedCornersTransformation(40, 0, RoundedCornersTransformation.CornerType.TOP),
          )
        )
      )
      .into(binding.imageViewPortada)

    Glide.with(requireContext())
      .load(restaurante.logo)
      .apply(
        RequestOptions.bitmapTransform(
          MultiTransformation(
            CenterCrop(),
            CircleCrop()
          )
        )
      )
      .into(binding.imageViewLogo)

    if (
      mainActivityViewModel.usuario.value!!.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante.id)
      ||
      mainActivityViewModel.usuario.value!!.detalleUsuario!!.idsRestaurantesFavoritos.contains(restaurante.idRestaurante)
    ) {
      binding.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
      binding.botonFavorito.isChecked = true
    } else {
      binding.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
      binding.botonFavorito.isChecked = false
    }
    binding.botonFavorito.setOnClickListener {
      if (binding.botonFavorito.isChecked) {
        binding.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
        hacerFavorito(restaurante)
      } else {
        binding.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
        eliminarFavorito(restaurante)
      }
    }
  }

  /**
   * Agrega un restaurante a favoritos
   *
   * @param restaurante Restaurante a hacer favorito
   * @author Chort Julio
   */
  private fun hacerFavorito(restaurante: Restaurante) {
    val restauranteFavorito = toRestauranteUsuario(restaurante)
    restauranteFavorito.idUsuario = mainActivityViewModel.usuario.value!!.id
    restauranteFavoritoRepository.guardar(restauranteFavorito, mainActivityViewModel.usuario.value!!.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        mainActivityViewModel.usuario.value!!.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
      }

      override fun onError(exception: Throwable) {}
    })
  }

  /**
   * Elimina un restaurante de favoritos
   *
   * @param restaurante Restaurante a eliminar
   * @author Chort Julio
   */
  private fun eliminarFavorito(restaurante: Restaurante) {
    restauranteFavoritoRepository.borrar(restaurante.id, mainActivityViewModel.usuario.value!!.id, mainActivityViewModel.usuario.value!!.idDetalleUsuario, object : FirestoreCallback<List<String>> {
      override fun onSuccess(result: List<String>) {
        mainActivityViewModel.usuario.value!!.detalleUsuario!!.idsRestaurantesFavoritos = HashSet(result)
      }

      override fun onError(exception: Throwable) {}
    })
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
   * Muestra el layout que contiene el mapa
   *
   * @author Federico Quarin
   */
  private fun ocultarLayoutPermisos() {
    binding.pedirPermisosLayout.visibility = View.INVISIBLE
  }

  /**
   * Activa o desactiva el overlay de carga segun el parametro [loading]
   *
   * @author Federico Quarin
   */
  private fun setLoading(loading: Boolean) {
    if (loading)
      binding.loadingOverlay.visibility = View.VISIBLE
    else
      binding.loadingOverlay.visibility = View.GONE
  }
}
