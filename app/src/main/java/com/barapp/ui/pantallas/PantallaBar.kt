package com.barapp.ui.pantallas

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import com.barapp.R
import com.barapp.databinding.FragmentPantallaBarBinding
import com.barapp.databinding.OpinionLayoutBinding
import com.barapp.model.EstadoRestaurante
import com.barapp.model.Opinion
import com.barapp.ui.AuthActivity
import com.barapp.ui.MainActivity
import com.barapp.util.Interpolator
import com.barapp.viewModels.sharedViewModels.BarAReservarSharedViewModel
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaBarViewModel
import com.barapp.viewModels.sharedViewModels.PantallaOpinionesSharedViewModel
import com.barapp.viewModels.sharedViewModels.RestauranteSeleccionadoSharedViewModel
import com.barapp.viewModels.sharedViewModels.UbicacionBarSharedViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import java.text.SimpleDateFormat
import java.util.*

/**
 * Esta clase es un [Fragment] que se utiliza para mostrar un bar en especifico. A traves del mismo
 * se puede acceder a las funciones de:
 * - Realizar una reserva en el bar (no implementado)
 * - Ver el menu del bar (no implementado)
 * - Ver la ubicacion del bar en un mapa (no implementado)
 * - Agregar/quitar de favoritos (no implementado)
 *
 * @author Federico Quarin
 */
class PantallaBar : Fragment() {
  companion object {
    const val ORIGEN_RESULTADOS_BUSQUEDA = 1
    const val ORIGEN_PANTALLA_PRINCIPAL = 2
  }

  private val ubicacionViewModel: UbicacionBarSharedViewModel by
  navGraphViewModels(R.id.pantallaBar)

  private val viewModel: PantallaBarViewModel by viewModels {
    PantallaBarViewModel.Factory(
      barSeleccionadoViewModel.restaurante!!,
      mainActivityViewModel.usuario.value!!,
    )
  }

  private lateinit var barSeleccionadoViewModel: RestauranteSeleccionadoSharedViewModel

  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  private val barAReservarViewModel: BarAReservarSharedViewModel by
  navGraphViewModels(R.id.pantallaNavegacionPrincipal)

  private val opinionesSharedViewModel: PantallaOpinionesSharedViewModel by
  navGraphViewModels(R.id.pantallaBar)

  private lateinit var binding: FragmentPantallaBarBinding

  private var toolbarEsVisible: Boolean = false
  private var botonMenuHabilitado: Boolean = false

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaBarBinding.inflate(inflater, container, false)

    binding.root.transitionName = arguments?.getString("transition_name")

    // Se setean las transiciones desde restaurante, tanto de entrada como de retorno
    sharedElementEnterTransition = MaterialContainerTransform()

    exitTransition = null

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.postponeEnterTransition()

    /*
       Se comprueba el origen desde donde se llama al fragmento, para saber quien
       tiene el view model que contiene el restaurante a mostrar
    */
    arguments?.getInt("origen").let {
      val navController = NavHostFragment.findNavController(this)
      val origen =
        when (it) {
          ORIGEN_RESULTADOS_BUSQUEDA -> R.id.pantallaResultadosBusqueda
          ORIGEN_PANTALLA_PRINCIPAL -> R.id.pantallaNavegacionPrincipal
          else -> 0
        }
      val backStackEntry = navController.getBackStackEntry(origen)

      barSeleccionadoViewModel =
        ViewModelProvider(backStackEntry)[RestauranteSeleccionadoSharedViewModel::class.java]
    }

    mainActivityViewModel.guardarRestauranteVistoRecientemente(barSeleccionadoViewModel.restaurante!!)

    ubicacionViewModel.ubicacionRestaurante = viewModel.restaurante.ubicacion
    ubicacionViewModel.nombreRestaurante = viewModel.restaurante.nombre
    ubicacionViewModel.logo = viewModel.restaurante.logo

    binding.textViewNombre.text = viewModel.restaurante.nombre
    binding.textViewDireccion.text =
      getString(
        R.string.cardview_texto_direccion,
        viewModel.restaurante.ubicacion.calle,
        viewModel.restaurante.ubicacion.numero,
      )
    binding.txtViewPuntuacionRestaurante.text =
      viewModel.restaurante.puntuacion.toString().substring(0, 3)
    binding.ratingBarPuntuacion.rating = viewModel.restaurante.puntuacion.toFloat()
    "(${viewModel.restaurante.cantidadOpiniones})".also {
      binding.txtViewCantidadOpiniones.text = it
    }

    Glide.with(requireContext())
      .load(viewModel.restaurante.logo)
      .apply(RequestOptions.circleCropTransform())
      .into(binding.imageViewLogo)
    Glide.with(requireContext())
      .load(viewModel.restaurante.portada)
      .apply(RequestOptions.centerCropTransform())
      .listener(onImageLoadedRequestListener())
      .into(binding.imageViewFoto)

    barSeleccionadoViewModel.distancia?.let {
      binding.textViewDistancia.text = getString(R.string.cardview_texto_distancia, it)
    } ?: run { binding.textViewDistancia.visibility = View.INVISIBLE }

    barSeleccionadoViewModel.restaurante?.estado.let {
      binding.fabReservar.isEnabled = it == EstadoRestaurante.HABILITADO

      if (it == EstadoRestaurante.PAUSADO) {
        binding.txtViewPaused.visibility = View.VISIBLE
        binding.imageViewFoto.alpha = 0.5f
      } else {
        binding.txtViewPaused.visibility = View.GONE
        binding.imageViewFoto.alpha = 1.0f
      }
    }

    barSeleccionadoViewModel.restaurante?.detalleRestaurante?.let { detalle ->

      if (detalle.descripcion.isEmpty()) {
        binding.textViewDescripcion.visibility = View.GONE
        binding.textViewDescripcionVacia.visibility = View.VISIBLE
      } else {
        binding.textViewDescripcion.text = detalle.descripcion
        binding.textViewDescripcion.visibility = View.VISIBLE
        binding.textViewDescripcionVacia.visibility = View.GONE
      }

      val url = Uri.parse(detalle.menu)
      if (detalle.menu.isNotEmpty() && URLUtil.isValidUrl(url.toString())) {
        binding.botonMenu.isEnabled = true
        botonMenuHabilitado = true
        if (toolbarEsVisible) mostrarBotonesToolbar()
      } else {
        binding.botonMenu.isEnabled = false
      }

      when (detalle.opiniones.size) {
        0 -> {
          binding.opinion1.opinionLayout.visibility = View.GONE
          binding.opinion2.opinionLayout.visibility = View.GONE
          binding.botonVerMasOpiniones.visibility = View.GONE
          binding.linearLayoutOpiniones.visibility = View.GONE
        }

        1 -> {
          setearOpinion(detalle.opiniones[0], binding.opinion1)
          binding.labelNoHayOpiniones.visibility = View.GONE
          binding.opinion2.opinionLayout.visibility = View.GONE
        }

        else -> {
          setearOpinion(detalle.opiniones[0], binding.opinion1)
          setearOpinion(detalle.opiniones[1], binding.opinion2)
          binding.labelNoHayOpiniones.visibility = View.GONE
        }
      }
    }

    viewModel.loading.observe(viewLifecycleOwner) {
      setLoading(it)
    }

    viewModel.error.observe(viewLifecycleOwner) {
      it?.run {
        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        viewModel.errorMostrado()
      }
    }

    viewModel.loadingReservasPendientes.observe(viewLifecycleOwner) { loading ->
      binding.fabReservar.isEnabled = !loading
    }

    viewModel.alcanzoLimiteReservas.observe(viewLifecycleOwner) { alcanzoLimite ->
      if (!alcanzoLimite)
        binding.fabReservar.setOnClickListener { mostrarPantallaReservar() }
      else
        binding.fabReservar.setOnClickListener {
          Toast.makeText(
            requireContext(),
            resources.getString(R.string.error_limite_reservas_alcanzado, 3),
            Toast.LENGTH_SHORT
          ).show()
        }
    }

    /*
       Se setea un scrollListener donde se define si mostrar u ocultar
       los botones de la toolbar, en base a si se scrolleo y se han ocultado los botones
    */
    binding.nestedScrollView.setOnScrollChangeListener(
      NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
        if (!toolbarEsVisible && scrollY > 0) {
          toolbarEsVisible = true
          mostrarBotonesToolbar()
        } else if (toolbarEsVisible && scrollY == 0) {
          toolbarEsVisible = false
          ocultarBotonesToolbar()
        }
      }
    )

    binding.linearLayoutOpiniones.setOnClickListener { mostrarMasOpiniones() }
    binding.botonMenu.setOnClickListener { mostrarMenu() }
    binding.botonUbicacion.setOnClickListener { mostrarUbicacion() }
    binding.botonVerMasOpiniones.setOnClickListener { mostrarMasOpiniones() }

    if (viewModel.esFavorito()) {
      binding.botonFavorito.isChecked = true
      binding.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
    } else {
      binding.botonFavorito.isChecked = false
      binding.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
    }

    binding.botonFavorito.setOnClickListener { cambiarFavorito() }

    binding.toolbar.addMenuProvider(
      object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
          // noop
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
          return onItemSelected(menuItem)
        }
      }
    )

    binding.toolbar.setNavigationOnClickListener { volverAtras() }
    binding.botonAtras.setOnClickListener { volverAtras() }
  }

  override fun onResume() {
    super.onResume()
    binding.ratingBarPuntuacion.rating = barSeleccionadoViewModel.restaurante!!.puntuacion.toFloat()
  }

  private fun mostrarBotonesToolbar() {
    binding.toolbar.navigationIcon =
      ResourcesCompat.getDrawable(resources, R.drawable.icon_outlined_arrow_back_24, null)

    binding.toolbar.menu.findItem(R.id.item_ubicacion).isVisible = true
    binding.toolbar.menu.findItem(R.id.item_favorito).isVisible = true
    if (botonMenuHabilitado) binding.toolbar.menu.findItem(R.id.item_menu).isVisible = true
  }

  private fun ocultarBotonesToolbar() {
    binding.toolbar.navigationIcon = null
    binding.toolbar.menu.findItem(R.id.item_menu).isVisible = false
    binding.toolbar.menu.findItem(R.id.item_ubicacion).isVisible = false
    binding.toolbar.menu.findItem(R.id.item_favorito).isVisible = false
  }

  private fun volverAtras() {
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun mostrarMenu() {
    barSeleccionadoViewModel.restaurante?.detalleRestaurante?.let {
      if (it.menu.isNotEmpty()) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.menu))
        startActivity(browserIntent)
      }
    }
  }

  private fun mostrarUbicacion() {
    exitTransition =
      MaterialSharedAxis(MaterialSharedAxis.X, true)
        .setInterpolator(Interpolator.emphasizedInterpolator())
        .excludeTarget(R.id.fabReservar, true)
    reenterTransition =
      MaterialSharedAxis(MaterialSharedAxis.X, false)
        .setInterpolator(Interpolator.emphasizedInterpolator())
        .excludeTarget(R.id.fabReservar, true)

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaBar_to_pantallaUbicacionBar)
  }

  private fun cambiarFavorito() {
    if (binding.botonFavorito.isChecked) {
      // Logica de agregar favorito
      binding.botonFavorito.setIconResource(R.drawable.icon_filled_favorite_24)
      viewModel.hacerFavorito()
    } else {
      // Logica de remover favorito
      binding.botonFavorito.setIconResource(R.drawable.icon_outlined_favorite_24)
      viewModel.eliminarFavorito()
    }
  }

  private fun onItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.item_menu -> mostrarMenu()
      R.id.item_ubicacion -> mostrarUbicacion()
      R.id.item_favorito -> cambiarFavorito()
      else -> return false
    }

    return true
  }

  private fun mostrarPantallaReservar() {
    val extras = FragmentNavigatorExtras(binding.fabReservar to "transition_pantalla_reservar")

    exitTransition = Hold()
    reenterTransition = null

    // Copiar el bar y el detalle para crear la reserva
    barAReservarViewModel.barSeleccionado = barSeleccionadoViewModel.restaurante!!
    barAReservarViewModel.detalleBarSeleccionado =
      barSeleccionadoViewModel.restaurante!!.detalleRestaurante!!

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaBar_to_pantallaCrearReserva, null, null, extras)
  }

  /**
   * Este metodo se utiliza para crear un [RequestListener], el cual es utilizado como listener de
   * la carga de fotos en [Glide]. En este caso, el listener inicia la transicion pospuesta
   * previamente, tanto si la carga es exitosa como en caso contrario
   *
   * @author Federico Quarin
   */
  private fun onImageLoadedRequestListener(): RequestListener<Drawable> {
    return object : RequestListener<Drawable> {
      override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean,
      ): Boolean {
        this@PantallaBar.startPostponedEnterTransition()
        return false
      }

      override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean,
      ): Boolean {
        this@PantallaBar.startPostponedEnterTransition()
        return false
      }
    }
  }

  private fun mostrarMasOpiniones() {
    opinionesSharedViewModel.nombreRestauranteSeleccionado =
      barSeleccionadoViewModel.restaurante!!.nombre
    opinionesSharedViewModel.idRestauranteSeleccionado = barSeleccionadoViewModel.restaurante!!.id
    opinionesSharedViewModel.caracteristicasRestauranteSeleccionado =
      barSeleccionadoViewModel.restaurante!!.detalleRestaurante!!.caracteristicas
    opinionesSharedViewModel.puntuacionRestauranteSeleccionado =
      barSeleccionadoViewModel.restaurante!!.puntuacion

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaBar_to_pantallaOpiniones)
  }

  private fun setearOpinion(opinion: Opinion, binding: OpinionLayoutBinding) {
    binding.textViewUsuario.text =
      getString(
        R.string.pantalla_bar_texto_nombre_usuario,
        opinion.usuario.nombre,
        opinion.usuario.apellido,
      )
    binding.textViewFecha.text = getFechaFormateada(opinion.fecha)
    binding.textViewOpinion.text =
      getString(R.string.pantalla_bar_texto_opinion, opinion.comentario)
    binding.ratingBarPuntuacion.rating = opinion.nota.toFloat()
    binding.textViewCantidadPersonas.text =
      getTextoCantidadPersonasOpinion(opinion.cantidadPersonas)
    "(${opinion.horario.tipoComida})".also { binding.textViewTipoComida.text = it }
    Glide.with(requireContext())
      .load(opinion.usuario.foto)
      .apply(RequestOptions.circleCropTransform())
      .into(binding.imageViewFotoPerfil)
  }

  private fun setLoading(loading: Boolean) {
    (activity as MainActivity).setLoading(loading)
  }

  private fun getTextoCantidadPersonasOpinion(cantidadPersonas: Int): String {
    return "Reservó para " + if (cantidadPersonas == 1) {
      "1 persona"
    } else {
      "$cantidadPersonas personas"
    }
  }

  private fun getFechaFormateada(fecha: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    val date = inputFormat.parse(fecha)
    return outputFormat.format(date!!)
  }
}
