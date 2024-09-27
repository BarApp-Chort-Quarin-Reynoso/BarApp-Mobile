package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.barapp.R
import com.barapp.databinding.FragmentPantallaPrincipalBinding
import com.barapp.model.Restaurante
import com.barapp.ui.MainActivity
import com.barapp.util.interfaces.OnRestauranteClicked
import com.barapp.util.interfaces.OnSnackbarShowed
import com.barapp.ui.recyclerViewAdapters.HorizontalRecyclerViewAdapter
import com.barapp.util.Interpolator
import com.barapp.util.interfaces.LoadingHandler
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaPrincipalViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough

class PantallaPrincipal :
  Fragment(), HorizontalRecyclerViewAdapter.ActualizarFavoritos, OnSnackbarShowed, LoadingHandler {
  companion object {
    const val ID_CERCA_DE_TI = "1"
    const val ID_VISTOS_RECIENTEMENTE = "2"
    const val ID_DESTACADOS = "3"
  }

  private lateinit var binding: FragmentPantallaPrincipalBinding

  private val pantallaPrincipalViewModel: PantallaPrincipalViewModel by viewModels {
    PantallaPrincipalViewModel.Factory()
  }

  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  private lateinit var onFabBuscarClickedListener: OnFabBuscarClicked
  private lateinit var onRestauranteClicked: OnRestauranteClicked

  private var cercaDeTiRecyclerAdapter: HorizontalRecyclerViewAdapter? = null
  private var adapterVistosRecientemente: HorizontalRecyclerViewAdapter? = null
  private var adapterRestaurantesDestacados: HorizontalRecyclerViewAdapter? = null

  private var showSnackbarReservaExitosa = false

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
    binding = FragmentPantallaPrincipalBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Se pospone la transicion de entrada para que funcione la transicion al volver
    postponeEnterTransition()
    OneShotPreDrawListener.add(view) { startPostponedEnterTransition() }

    onFabBuscarClickedListener = requireActivity() as OnFabBuscarClicked
    onRestauranteClicked =
      (parentFragment as NavHostFragment).parentFragment as OnRestauranteClicked

    mainActivityViewModel.usuario.observe(viewLifecycleOwner) { usuario ->
      pantallaPrincipalViewModel.usuario = usuario

      if (adapterRestaurantesDestacados == null) {
        adapterRestaurantesDestacados =
          HorizontalRecyclerViewAdapter(
            ArrayList(),
            usuario,
            this,
            ID_DESTACADOS,
            object : HorizontalRecyclerViewAdapter.OnItemClickListener {
              override fun onClick(
                transitionView: View,
                restaurante: Restaurante,
                distancia: Int?,
              ) {
                onRestauranteClicked.onRestauranteClicked(restaurante, transitionView, distancia)
              }
            },
            this
          )
      }

      if (cercaDeTiRecyclerAdapter == null) {
        cercaDeTiRecyclerAdapter =
          HorizontalRecyclerViewAdapter(
            ArrayList(),
            usuario,
            this,
            ID_CERCA_DE_TI,
            object : HorizontalRecyclerViewAdapter.OnItemClickListener {
              override fun onClick(
                transitionView: View,
                restaurante: Restaurante,
                distancia: Int?,
              ) {
                onRestauranteClicked.onRestauranteClicked(restaurante, transitionView, distancia)
              }
            },
            this
          )
      }

      if (adapterVistosRecientemente == null) {
        adapterVistosRecientemente =
          HorizontalRecyclerViewAdapter(
            ArrayList(),
            usuario,
            this,
            ID_VISTOS_RECIENTEMENTE,
            object : HorizontalRecyclerViewAdapter.OnItemClickListener {
              override fun onClick(
                transitionView: View,
                restaurante: Restaurante,
                distancia: Int?,
              ) {
                onRestauranteClicked.onRestauranteClicked(restaurante, transitionView, distancia)
              }
            },
            this
          )
      }

      binding.recyclerViewHorizontalRestaurantesDestacados.layoutManager =
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
      binding.recyclerViewHorizontalRestaurantesDestacados.adapter = adapterRestaurantesDestacados

      binding.recyclerViewHorizontalCercaDeTi.layoutManager =
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
      binding.recyclerViewHorizontalCercaDeTi.adapter = cercaDeTiRecyclerAdapter

      binding.recyclerViewHorizontalVistosRecientemente.layoutManager =
        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
      binding.recyclerViewHorizontalVistosRecientemente.adapter = adapterVistosRecientemente

      pantallaPrincipalViewModel.buscarRestaurantesVistosRecientemente()
      pantallaPrincipalViewModel.buscarRestaurantesDestacados()
      pantallaPrincipalViewModel.buscarRestaurantesCercaDeTi()
    }

    mainActivityViewModel.ubicacionUsuario.observe(viewLifecycleOwner) { ubicacion ->
      pantallaPrincipalViewModel.ubicacionDisponible(ubicacion)

      mainActivityViewModel.usuario.observe(viewLifecycleOwner) {
        pantallaPrincipalViewModel.listaRestaurantesDestacados.observe(viewLifecycleOwner) {
          pantallaPrincipalViewModel.calcularDistanciasDestacados()
        }

        pantallaPrincipalViewModel.listaRestaurantesCercaDeTi.observe(viewLifecycleOwner) {
          pantallaPrincipalViewModel.calcularDistanciasCercaDeTi()
        }

        pantallaPrincipalViewModel.listaRestaurantesVistosRecientemente.observe(
          viewLifecycleOwner
        ) {
          pantallaPrincipalViewModel.calcularDistanciasVistosRecientemente()
        }
      }
    }

    pantallaPrincipalViewModel.listaRestaurantesDestacados.observe(viewLifecycleOwner) {
      listaRestaurante ->
        cargarRecyclerViewRestaurantesDestacados(listaRestaurante)
    }

    pantallaPrincipalViewModel.distanciasDestacados.observe(viewLifecycleOwner) {
      cargarDistanciasRecyclerViewDestacados(it)
    }

    pantallaPrincipalViewModel.listaRestaurantesCercaDeTi.observe(viewLifecycleOwner) {
      listaRestaurante -> if (listaRestaurante.isEmpty()) {
          binding.labelCercaDeTi.visibility = View.GONE
          binding.recyclerViewHorizontalCercaDeTi.visibility = View.GONE
        } else {
          cargarRecyclerViewCercaDeTi(listaRestaurante)
          binding.labelCercaDeTi.visibility = View.VISIBLE
          binding.recyclerViewHorizontalCercaDeTi.visibility = View.VISIBLE
      }
    }

    pantallaPrincipalViewModel.distanciasCercaDeTi.observe(viewLifecycleOwner) {
      cargarDistanciasRecyclerViewCercaDeTi(it)
    }

    pantallaPrincipalViewModel.listaRestaurantesVistosRecientemente.observe(viewLifecycleOwner) {
      listaRestaurante ->
      if (listaRestaurante.isEmpty()) {
        binding.labelVistoRecientemente.visibility = View.GONE
        binding.recyclerViewHorizontalVistosRecientemente.visibility = View.GONE
      } else {
        cargarRecyclerViewVistosRecientemente(listaRestaurante)
        binding.labelVistoRecientemente.visibility = View.VISIBLE
        binding.recyclerViewHorizontalVistosRecientemente.visibility = View.VISIBLE
      }
    }

    pantallaPrincipalViewModel.distanciasVistosRecientemente.observe(viewLifecycleOwner) {
      cargarDistanciasRecyclerViewVistosRecientemente(it)
    }

    pantallaPrincipalViewModel.error.observe(viewLifecycleOwner) {
      if (it != null) {
        Snackbar.make(requireView(), "Error: " + it.cause, Snackbar.LENGTH_LONG).show()
        it.printStackTrace()
      }
    }

    // Floating Action Button
    binding.floatingActionButtonBuscar.setOnClickListener {
      onFabBuscarClickedListener.onFabBuscarClicked(it)
    }

    if (showSnackbarReservaExitosa) {
      showSnackbarReservaExitosa = false

      Snackbar.make(
          requireView(),
          getString(R.string.pantalla_confirmacion_reservas_snackbar_texto),
          Snackbar.LENGTH_LONG,
        )
        .setDuration(Snackbar.LENGTH_LONG)
        .show()
    }
  }

  private fun cargarRecyclerViewRestaurantesDestacados(restaurantes: List<Restaurante>) {
    adapterRestaurantesDestacados?.updateRestaurantesItems(restaurantes)
  }

  private fun cargarRecyclerViewCercaDeTi(restaurantes: List<Restaurante>) {
    cercaDeTiRecyclerAdapter?.updateRestaurantesItems(restaurantes)
  }

  private fun cargarRecyclerViewVistosRecientemente(restaurantes: List<Restaurante>) {
    adapterVistosRecientemente?.updateRestaurantesItems(restaurantes)
  }

  private fun cargarDistanciasRecyclerViewCercaDeTi(distancias: HashMap<String, Int?>) {
    cercaDeTiRecyclerAdapter?.setDistancias(distancias)
  }

  private fun cargarDistanciasRecyclerViewDestacados(distancias: HashMap<String, Int?>) {
    adapterRestaurantesDestacados?.setDistancias(distancias)
  }

  private fun cargarDistanciasRecyclerViewVistosRecientemente(distancias: HashMap<String, Int?>) {
    adapterVistosRecientemente?.setDistancias(distancias)
  }

  interface OnFabBuscarClicked {
    fun onFabBuscarClicked(fabBuscar: View)
  }

  override fun actualizarFavoritos(holder: HorizontalRecyclerViewAdapter.RestaurantesViewHolder, idRestaurante: String) {
    adapterRestaurantesDestacados?.addToFavorites(holder, idRestaurante)
    cercaDeTiRecyclerAdapter?.addToFavorites(holder, idRestaurante)
    adapterVistosRecientemente?.addToFavorites(holder, idRestaurante)
  }

  override fun showSnackbar(snackbar: Snackbar) {
    showSnackbarReservaExitosa = true
  }

  override fun setLoading(loading: Boolean) {
    (activity as MainActivity).setLoading(loading)
  }
}
