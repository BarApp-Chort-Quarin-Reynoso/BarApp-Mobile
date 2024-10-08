package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.barapp.R
import com.barapp.databinding.BottomShiftFiltersBinding
import com.barapp.databinding.FragmentPantallaResultadosBusquedaBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.MainActivity
import com.barapp.ui.recyclerViewAdapters.ResultadosRestauranteRecyclerAdapter
import com.barapp.util.interfaces.LoadingHandler
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaResultadosBusquedaViewModel
import com.barapp.viewModels.sharedViewModels.RestauranteSeleccionadoSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis

class PantallaResultadosBusqueda : Fragment(), ResultadosRestauranteRecyclerAdapter.Callbacks,
  LoadingHandler {

  private lateinit var binding: FragmentPantallaResultadosBusquedaBinding
  private lateinit var bottomSheetDialog: BottomSheetDialog

  private val pantallaResultadosBusquedaViewModel: PantallaResultadosBusquedaViewModel by
    viewModels()

  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  private val restauranteSeleccionadoViewModel: RestauranteSeleccionadoSharedViewModel by
    navGraphViewModels(R.id.pantallaResultadosBusqueda)

  private var adapter: ResultadosRestauranteRecyclerAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentPantallaResultadosBusquedaBinding.inflate(inflater, container, false)
    val filtersBinding = BottomShiftFiltersBinding.inflate(inflater, container, false)

    // Initialize the BottomSheetDialog
    bottomSheetDialog = BottomSheetDialog(requireContext())
    bottomSheetDialog.setContentView(filtersBinding.root)

    // Inflate the bottom_shift_filters.xml layout
    val bottomShiftFiltersView = inflater.inflate(R.layout.bottom_shift_filters, container, false)

    // Add the bottom_shift_filters.xml layout to the parent layout
    binding.parentLayout.addView(bottomShiftFiltersView)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Inflate your custom layout
    val bottomShiftFiltersView = layoutInflater.inflate(R.layout.bottom_shift_filters, null)

    // Set your custom layout as the content view of the BottomSheetDialog
    bottomSheetDialog.setContentView(bottomShiftFiltersView)


    binding.listaVacia.textListaVacia.text = getString(R.string.lista_vacia_resultados_busqueda)
    binding.listaVacia.textListaVacia.visibility = View.GONE
    binding.listaVacia.logoBarApp.visibility = View.GONE

    // Se pospone la transicion de entrada para que funcione la transicion al volver
    postponeEnterTransition()
    OneShotPreDrawListener.add(view, this::startPostponedEnterTransition)

    pantallaResultadosBusquedaViewModel.buscarRestaurantesSegunTexto(
      arguments?.getString("textoBusqueda") ?: ""
    )

    pantallaResultadosBusquedaViewModel.listaRestaurantes.observe(viewLifecycleOwner) {
      listaRestaurantes: List<Restaurante> ->
      mainActivityViewModel.usuario.observe(viewLifecycleOwner) { usuario: Usuario ->
        mainActivityViewModel.ubicacionUsuario.observe(viewLifecycleOwner) { ubicacion ->
          pantallaResultadosBusquedaViewModel.ubicacionDisponible(ubicacion)
          pantallaResultadosBusquedaViewModel.calcularDistancias()
        }

        cargarRecyclerView(listaRestaurantes, usuario)
      }
    }

    pantallaResultadosBusquedaViewModel.distancias.observe(viewLifecycleOwner) {
      cargarDistanciasRecyclerView(it)
    }

    binding.botonSwap.setOnClickListener {
//      pantallaResultadosBusquedaViewModel.ordenarPorDistancia()
        pantallaResultadosBusquedaViewModel.ordenarPorRating()
    }

    binding.botonFiltros.setOnClickListener {
      // Show the BottomSheetDialog when the button is clicked
      bottomSheetDialog.show()
    }

    binding.toolbar.setNavigationOnClickListener { volverAtras() }
  }

  fun onClickResultado(transitionView: View, restaurante: Restaurante, distancia: Int?) {
    val bundle = Bundle()
    bundle.putString("transition_name", restaurante.id)
    bundle.putInt("origen", PantallaBar.ORIGEN_RESULTADOS_BUSQUEDA)

    val extras = FragmentNavigatorExtras(transitionView to transitionView.transitionName)

    restauranteSeleccionadoViewModel.setSyncRestaurante(restaurante)
    restauranteSeleccionadoViewModel.distancia = distancia

    exitTransition = Hold()

    NavHostFragment.findNavController(this)
      .navigate(R.id.action_pantallaResultadosBusqueda_to_pantallaBar, bundle, null, extras)
  }

  private fun volverAtras() {
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun cargarRecyclerView(restaurantes: List<Restaurante>, usuario: Usuario) {
    val recyclerView = binding.recyclerViewRestaurantes

    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    adapter =
      ResultadosRestauranteRecyclerAdapter(
        ArrayList(restaurantes),
        usuario,
        this,
        object : ResultadosRestauranteRecyclerAdapter.OnItemClickListener {
          override fun onClick(transitionView: View, restaurante: Restaurante, distancia: Int?) {
            onClickResultado(transitionView, restaurante, distancia)
          }
        },
        this
      )

    recyclerView.adapter = adapter
    recyclerView.isClickable = true

    actualizarCantidadRestaurantes(adapter!!.itemCount)
  }

  private fun cargarDistanciasRecyclerView(distancias: HashMap<String, Int?>) {
    adapter?.setDistancias(distancias)
  }

  override fun actualizarCantidadRestaurantes(cantRestaurantes: Int) {
    binding.txtViewCantidadResultadosEncontrados.text =
      resultadosEncontradosPluralOSingular(cantRestaurantes)
    if (cantRestaurantes == 0) {
      binding.listaVacia.textListaVacia.visibility = View.VISIBLE
      binding.listaVacia.logoBarApp.visibility = View.VISIBLE
    } else {
      binding.listaVacia.textListaVacia.visibility = View.GONE
      binding.listaVacia.logoBarApp.visibility = View.GONE
    }
  }

  private fun resultadosEncontradosPluralOSingular(cant: Int): String {
    return if (cant == 1) getString(R.string.placeholder_restaurante_encontrado, cant)
    else getString(R.string.placeholder_restaurantes_encontrados, cant)
  }

  override fun setLoading(loading: Boolean) {
    (activity as MainActivity).setLoading(loading)
  }
}
