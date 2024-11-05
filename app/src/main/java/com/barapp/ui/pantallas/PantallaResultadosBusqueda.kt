package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.barapp.R
import com.barapp.databinding.FragmentPantallaResultadosBusquedaBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.MainActivity
import com.barapp.ui.recyclerViewAdapters.ResultadosRestauranteRecyclerAdapter
import com.barapp.util.interfaces.LoadingHandler
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaResultadosBusquedaViewModel
import com.barapp.viewModels.sharedViewModels.RestauranteSeleccionadoSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis

class PantallaResultadosBusqueda : Fragment(), ResultadosRestauranteRecyclerAdapter.Callbacks,
  LoadingHandler {

  private lateinit var binding: FragmentPantallaResultadosBusquedaBinding

  private val pantallaResultadosBusquedaViewModel: PantallaResultadosBusquedaViewModel by
  viewModels()

  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  private val restauranteSeleccionadoViewModel: RestauranteSeleccionadoSharedViewModel by
  navGraphViewModels(R.id.pantallaResultadosBusqueda)

  private lateinit var adapter: ResultadosRestauranteRecyclerAdapter

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
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val standardBottomSheet = binding.bottomSheet
    val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
    standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

    binding.listaVacia.textListaVacia.text = getString(R.string.lista_vacia_resultados_busqueda)

    pantallaResultadosBusquedaViewModel.buscarRestaurantesSegunTexto(
      arguments?.getString("textoBusqueda") ?: ""
    )

    pantallaResultadosBusquedaViewModel.listaRestaurantes.observe(viewLifecycleOwner) { listaRestaurantes ->
      mainActivityViewModel.usuario.observe(viewLifecycleOwner) { usuario: Usuario ->
        mainActivityViewModel.ubicacionUsuario.observe(viewLifecycleOwner) { ubicacion ->
          pantallaResultadosBusquedaViewModel.ubicacionDisponible(ubicacion)
          pantallaResultadosBusquedaViewModel.calcularDistancias()

          pantallaResultadosBusquedaViewModel.distancias.observe(viewLifecycleOwner) {
            cargarDistanciasRecyclerView(it)
          }
        }

        cargarRecyclerView(listaRestaurantes, usuario)
      }
    }

    binding.botonSwap.setOnClickListener {
      pantallaResultadosBusquedaViewModel.ordenarPorRating()
    }

    binding.botonFiltros.setOnClickListener {
      // Show the BottomSheetDialog when the button is clicked
      standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    binding.toolbar.setNavigationOnClickListener { volverAtras() }

    binding.chipGroupRatings.setOnCheckedChangeListener { group, checkedId ->
      binding.btnApply.isEnabled = checkedId != View.NO_ID
      pantallaResultadosBusquedaViewModel.minEstrellas = when (checkedId) {
        R.id.chip2estrellas -> 2
        R.id.chip3estrellas -> 3
        R.id.chip4estrellas -> 4
        else -> 0
      }
    }

    binding.btnApply.isEnabled = binding.chipGroupRatings.checkedChipId != View.NO_ID

    binding.btnApply.setOnClickListener {
      pantallaResultadosBusquedaViewModel.applyFilters()
      standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    binding.btnResetAll.setOnClickListener {
      pantallaResultadosBusquedaViewModel.applyFilters()
      binding.chipGroupRatings.clearCheck()
      standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
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
        this,
        object : ResultadosRestauranteRecyclerAdapter.OnItemClickListener {
          override fun onClick(transitionView: View, restaurante: Restaurante, distancia: Int?) {
            onClickResultado(transitionView, restaurante, distancia)
          }
        }
      )

    recyclerView.adapter = adapter
    recyclerView.isClickable = true

    actualizarCantidadRestaurantes(adapter.itemCount)
  }

  private fun cargarDistanciasRecyclerView(distancias: HashMap<String, Int?>) {
    adapter.setDistancias(distancias)
  }

  override fun actualizarCantidadRestaurantes(cantRestaurantes: Int) {
    binding.txtViewCantidadResultadosEncontrados.text =
      resultadosEncontradosPluralOSingular(cantRestaurantes)
    if (cantRestaurantes == 0) {
      binding.recyclerViewRestaurantes.visibility = View.GONE
    } else {
      binding.listaVacia.root.visibility = View.GONE
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
