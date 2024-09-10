package com.barapp.ui.pantallas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OneShotPreDrawListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.barapp.R
import com.barapp.databinding.FragmentPantallaOpinionesBinding
import com.barapp.ui.MainActivity
import com.barapp.ui.recyclerViewAdapters.CaracteristicasRecyclerAdapter
import com.google.android.material.transition.MaterialSharedAxis
import com.barapp.ui.recyclerViewAdapters.OpinionesRecyclerAdapter
import com.barapp.viewModels.PantallaOpinionesViewModel
import com.barapp.viewModels.sharedViewModels.PantallaOpinionesSharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PantallaOpiniones : Fragment() {

    private lateinit var binding: FragmentPantallaOpinionesBinding

    private val pantallaOpinionesViewModel: PantallaOpinionesViewModel by viewModels()

    private val opinionesSharedViewModel: PantallaOpinionesSharedViewModel by
    navGraphViewModels(R.id.pantallaBar)

    private var opinionesAdapter: OpinionesRecyclerAdapter? = null
    private var caracteristicasAdapter: CaracteristicasRecyclerAdapter? = null

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
        binding = FragmentPantallaOpinionesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        OneShotPreDrawListener.add(view, this::startPostponedEnterTransition)

        binding.toolbar.setNavigationOnClickListener { volverAtras() }

        binding.textViewPuntuacion.text = opinionesSharedViewModel.puntuacionRestauranteSeleccionado.toString().substring(0, 3)
        binding.ratingBarPuntuacion.rating = opinionesSharedViewModel.puntuacionRestauranteSeleccionado.toFloat()

        // Initialize the Caracteristicas RecyclerView
        val recyclerViewCaracteristicas = binding.recyclerViewCaracteristicas
        caracteristicasAdapter = CaracteristicasRecyclerAdapter(opinionesSharedViewModel.caracteristicasRestauranteSeleccionado)
        recyclerViewCaracteristicas.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerViewCaracteristicas.adapter = caracteristicasAdapter

        // Initialize the Opiniones RecyclerView
        val recyclerViewOpiniones = binding.recyclerViewOpiniones
        recyclerViewOpiniones.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        opinionesAdapter = OpinionesRecyclerAdapter(mutableListOf())
        recyclerViewOpiniones.adapter = opinionesAdapter

        pantallaOpinionesViewModel.buscarOpiniones(opinionesSharedViewModel.idRestauranteSeleccionado)

        pantallaOpinionesViewModel.opinionesRestaurante.observe(viewLifecycleOwner) { opiniones ->
            opinionesAdapter!!.updateOpiniones(opiniones)
            ("${opiniones.size} opiniones").also { binding.textViewCantidadOpiniones.text = it }
        }

        pantallaOpinionesViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                mostrarErrorAlgoSalioMal(requireContext().getString(R.string.error_buscando_opiniones_restaurante, opinionesSharedViewModel.nombreRestauranteSeleccionado))
            }
        }

        pantallaOpinionesViewModel.loading.observe(viewLifecycleOwner) { loading ->
            setLoading(loading)
        }
    }

    private fun setLoading(loading: Boolean) {
        (activity as MainActivity).setLoading(loading)
    }

    private fun mostrarErrorAlgoSalioMal(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.icon_baseline_sentiment_very_dissatisfied_24)
            .setTitle(requireContext().getString(R.string.error_titulo_algo_salio_mal))
            .setMessage(message)
            .setPositiveButton(requireContext().getString(R.string.boton_aceptar)) { _, _ ->
                volverAtras()
            }
            .show()
    }

    private fun volverAtras() {
        NavHostFragment.findNavController(this).popBackStack()
    }
}