package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.barapp.R
import com.barapp.databinding.FragmentPantallaMisFavoritosBinding
import com.barapp.model.Restaurante
import com.barapp.model.Usuario
import com.barapp.ui.MainActivity
import com.barapp.util.interfaces.OnRestauranteClicked
import com.barapp.ui.recyclerViewAdapters.ResultadosRestauranteRecyclerAdapter
import com.barapp.util.Interpolator
import com.barapp.util.interfaces.LoadingHandler
import com.barapp.viewModels.MainActivityViewModel
import com.barapp.viewModels.PantallaMisFavoritosViewModel
import com.faltenreich.skeletonlayout.Skeleton
import com.google.android.material.transition.MaterialFadeThrough

class PantallaMisFavoritos : Fragment(), ResultadosRestauranteRecyclerAdapter.Callbacks, LoadingHandler {

  private lateinit var binding: FragmentPantallaMisFavoritosBinding

  private lateinit var recyclerView: RecyclerView

  private val pantallaMisFavoritosViewModel: PantallaMisFavoritosViewModel by viewModels()

  private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

  private lateinit var onRestauranteClicked: OnRestauranteClicked

  private var adapter: ResultadosRestauranteRecyclerAdapter? = null

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
    binding = FragmentPantallaMisFavoritosBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.listaVacia.textListaVacia.text = getString(R.string.lista_vacia_mis_favoritos)
    binding.listaVacia.textListaVacia.visibility = View.GONE
    binding.listaVacia.logoBarApp.visibility = View.GONE

    recyclerView = binding.recyclerViewRestaurantes
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    recyclerView.isClickable = true

    mainActivityViewModel.usuario.observe(viewLifecycleOwner) { usuario: Usuario ->
      if (adapter == null) {
        adapter =
          ResultadosRestauranteRecyclerAdapter(
            ArrayList(),
            usuario,
            this,
            this,
            object : ResultadosRestauranteRecyclerAdapter.OnItemClickListener {
              override fun onClick(
                transitionView: View,
                restaurante: Restaurante,
                distancia: Int?,
              ) {
                onRestauranteClicked.onRestauranteClicked(restaurante, transitionView, distancia)
              }
            }
          )
      }

      recyclerView.adapter = adapter

      pantallaMisFavoritosViewModel.buscarFavoritos(usuario.id)
    }

    pantallaMisFavoritosViewModel.listaRestaurantesFavoritos.observe(viewLifecycleOwner) {
      listaRestaurantes: List<Restaurante> ->
      mainActivityViewModel.usuario.observe(viewLifecycleOwner) { usuario: Usuario ->
        cargarRestaurantes(listaRestaurantes, usuario)

        mainActivityViewModel.ubicacionUsuario.observe(viewLifecycleOwner) { ubicacion ->
          pantallaMisFavoritosViewModel.ubicacionDisponible(ubicacion)
          pantallaMisFavoritosViewModel.calcularDistancias()
        }
      }
    }

    pantallaMisFavoritosViewModel.cantidadFavoritos.observe(viewLifecycleOwner) {
      binding.txtViewCantidadResultadosEncontrados.text =
        String.format("%s%s", it, restFavoritosPluralOSingular(it))
    }

    pantallaMisFavoritosViewModel.distancias.observe(viewLifecycleOwner) { cargarDistancias(it) }

    pantallaMisFavoritosViewModel.loading.observe(viewLifecycleOwner) { loading ->
      (activity as MainActivity).setLoading(loading)
      if (loading) {
        binding.txtViewCantidadResultadosEncontrados.visibility = View.GONE
        binding.recyclerViewRestaurantes.visibility = View.GONE
      } else {
        binding.txtViewCantidadResultadosEncontrados.visibility = View.VISIBLE
        binding.recyclerViewRestaurantes.visibility = View.VISIBLE
      }
    }

    onRestauranteClicked =
      (parentFragment as NavHostFragment).parentFragment as OnRestauranteClicked

    binding.toolbar.setNavigationOnClickListener { volverAtras() }
  }

  private fun volverAtras() {
    NavHostFragment.findNavController(this).popBackStack()
  }

  private fun cargarRestaurantes(listaRestaurante: List<Restaurante>, usuario: Usuario) {
    adapter?.updateRestaurantesItems(listaRestaurante)

    actualizarCantidadRestaurantes(listaRestaurante.size)
  }

  private fun cargarDistancias(distancias: HashMap<String, Int?>) {
    adapter?.setDistancias(distancias)
  }

  override fun actualizarCantidadRestaurantes(cantRestaurantes: Int) {
    binding.txtViewCantidadResultadosEncontrados.text =
      String.format("%s%s", cantRestaurantes, restFavoritosPluralOSingular(cantRestaurantes))
    if (cantRestaurantes == 0) {
      binding.listaVacia.textListaVacia.visibility = View.VISIBLE
      binding.listaVacia.logoBarApp.visibility = View.VISIBLE
    } else {
      binding.listaVacia.textListaVacia.visibility = View.GONE
      binding.listaVacia.logoBarApp.visibility = View.GONE
    }
  }

  private fun restFavoritosPluralOSingular(cant: Int): String {
    return if (cant == 1) " restaurante favorito" else " restaurantes favoritos"
  }

  override fun setLoading(loading: Boolean) {
    (activity as MainActivity).setLoading(loading)
  }
}
